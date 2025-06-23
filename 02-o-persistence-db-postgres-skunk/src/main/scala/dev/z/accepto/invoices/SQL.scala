package dev.z
package accepto
package invoices

import scala.util.*

import neotype.unwrap
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*
import squants.market.*

import dev.z.accepto.organizations.SQL.*
import dev.z.accepto.users.auth.SQL.userId

object SQL:
  // --- Invoice field codecs ---
  lazy val invoiceId: Codec[InvoiceId] =
    uuid.imap(InvoiceId(_))(_.unwrap)

  lazy val moneyCodec: Codec[Money] =
    (numeric ~ varchar).imap {
      case (amount, currencyCode) =>
        Currency(currencyCode) match
          case Success(currency) => Money(amount, currency)
          case Failure(_) => throw NoSuchCurrencyException(currencyCode, defaultMoneyContext)
    }(money => (money.amount, money.currency.code))

  // --- Invoice Amount with Currency Codec ---
  lazy val invoiceAmount: Codec[InvoiceAmount] =
    moneyCodec.imap(InvoiceAmount(_))(_.unwrap)

  lazy val invoiceBuyer: Codec[InvoiceBuyer] =
    varchar.imap(InvoiceBuyer(_))(_.unwrap)

  lazy val invoiceSeller: Codec[InvoiceSeller] =
    varchar.imap(InvoiceSeller(_))(_.unwrap)

  lazy val invoiceDate: Codec[InvoiceDate] =
    date.imap(InvoiceDate(_))(_.unwrap)

  lazy val invoiceDueDate: Codec[InvoiceDueDate] =
    date.opt.imap(InvoiceDueDate(_))(_.unwrap)

  lazy val decoder: Decoder[Invoice] =
    (invoiceId *:
      organization *:
      invoiceAmount *:
      invoiceBuyer *:
      invoiceSeller *:
      invoiceDate *:
      invoiceDueDate).map:
      case id *: org *: amt *: buyer *: seller *: date *: due *: EmptyTuple =>
        Invoice(id, org, amt, buyer, seller, date, due)

  lazy val encoder: Encoder[Invoice] =
    (invoiceId *: organizationId *: invoiceAmount *: invoiceBuyer *: invoiceSeller *: invoiceDate *: invoiceDueDate)
      .contramap { inv =>
        inv.id *: inv
          .organization
          .id *: inv.amount *: inv.buyer *: inv.seller *: inv.date *: inv.dueDate *: EmptyTuple
      }

  lazy val createInvoiceEncoder: Encoder[(InvoiceId, OrganizationId, CreateInvoice)] =
    (invoiceId *: organizationId *: invoiceAmount *: invoiceBuyer *: invoiceSeller *: invoiceDate *: invoiceDueDate)
      .contramap:
        case (invId, orgId, ci) =>
          invId *: orgId *: ci.amount *: ci.buyer *: ci.seller *: ci.date *: ci.dueDate *: EmptyTuple
  lazy val insertInvoiceAsUser: Command[(OrganizationId, UserId, InvoiceId, CreateInvoice)] =
    sql"""
      WITH permission_check AS (
        SELECT 1
        FROM memberships
        WHERE user_id = $userId
          AND organization_id = $organizationId
          AND role IN ('Admin', 'Manager')
      )
      INSERT INTO invoices (id, organization_id, amount, currency_code, buyer, seller, date, due_date)
      SELECT $invoiceId, $organizationId, $invoiceAmount, $invoiceBuyer, $invoiceSeller, $invoiceDate, $invoiceDueDate
      WHERE EXISTS (SELECT 1 FROM permission_check)
    """
      .command
      .contramap:
        case (orgId, sessionUser, invoiceId, createInvoice) =>
          sessionUser *: orgId *: invoiceId *: orgId *: createInvoice.amount *: createInvoice.buyer *: createInvoice.seller *: createInvoice.date *: createInvoice.dueDate *: EmptyTuple

  def insertInvoicesAsUser(
    batchSize: Int
  ): Command[(OrganizationId, UserId, List[(InvoiceId, CreateInvoice)])] =
    sql"""
      WITH permission_check AS (
        SELECT 1
        FROM memberships
        WHERE user_id = $userId
          AND organization_id = $organizationId
          AND role IN ('Admin', 'Manager')
      )
      INSERT INTO invoices (id, organization_id, amount, currency_code, buyer, seller, date, due_date)
      SELECT * FROM (VALUES ${createInvoiceEncoder.values.list(batchSize)})
      AS new_invoices(id, organization_id, amount, currency_code, buyer, seller, date, due_date)
      WHERE EXISTS (SELECT 1 FROM permission_check)
    """
      .command
      .contramap:
        case (orgId, sessionUser, invoiceData) =>
          sessionUser *: orgId *: invoiceData.map {
            case (invId, ci) =>
              invId *: orgId *: ci *: EmptyTuple
          } *: EmptyTuple

  lazy val selectInvoice: Query[
    OrganizationId *: UserId *: InvoiceId *: EmptyTuple,
    Invoice,
  ] =
    sql"""
      SELECT i.id, o.id, o.name, i.amount, i.currency_code, i.buyer, i.seller, i.date, i.due_date
      FROM invoices AS i
      INNER JOIN organizations AS o ON i.organization_id = o.id
      INNER JOIN memberships AS m ON o.id = m.organization_id
      WHERE o.id = $organizationId AND m.user_id = $userId AND i.id = $invoiceId
    """.query(decoder)

  lazy val selectInvoices: Query[(OrganizationId, UserId), Invoice] =
    sql"""
      SELECT i.id, o.id, o.name, i.amount, i.currency_code, i.buyer, i.seller, i.date, i.due_date
      FROM invoices AS i
      INNER JOIN organizations AS o ON i.organization_id = o.id
      INNER JOIN memberships AS m ON o.id = m.organization_id
      WHERE o.id = $organizationId AND m.user_id = $userId
    """.query(decoder)

  lazy val selectInvoicesByDate: Query[(OrganizationId, UserId, InvoiceDate, InvoiceDate), Invoice] =
    sql"""
      SELECT i.id, o.id, o.name, i.amount, i.currency_code, i.buyer, i.seller, i.date, i.due_date
      FROM invoices AS i
      INNER JOIN organizations AS o ON i.organization_id = o.id
      INNER JOIN memberships AS m ON o.id = m.organization_id
      WHERE o.id = $organizationId
        AND m.user_id = $userId
        AND i.date BETWEEN $invoiceDate AND $invoiceDate
    """.query(decoder)

  def selectInvoicesByIds(
    batchSize: Int
  ): Query[(OrganizationId, UserId, List[InvoiceId]), Invoice] =
    sql"""
      SELECT i.id, o.id, o.name, i.amount, i.currency_code, i.buyer, i.seller, i.date, i.due_date
      FROM invoices AS i
      INNER JOIN organizations AS o ON i.organization_id = o.id
      INNER JOIN memberships AS m ON o.id = m.organization_id
      WHERE o.id = $organizationId AND m.user_id = $userId
      AND i.id IN (${invoiceId.list(batchSize)})
    """.query(decoder)
