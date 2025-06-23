package dev.z
package accepto
package invoices

import java.time.LocalDate
import java.util.UUID

import cats.*
import neotype.*
import squants.market.*

object InvoiceId extends Newtype[UUID]:
  def fromString(str: String): Either[String, InvoiceId] =
    try InvoiceId.make(UUID.fromString(str))
    catch case _: IllegalArgumentException => Left(s"Invalid UUID format: $str")

type InvoiceId = InvoiceId.Type

object InvoiceAmount extends Newtype[Money]
object InvoiceBuyer extends Newtype[String]
object InvoiceSeller extends Newtype[String]
object InvoiceDate extends Newtype[LocalDate]:
  def fromString(str: String): Either[String, InvoiceDate] =
    try InvoiceDate.make(LocalDate.parse(str))
    catch case _: java.time.format.DateTimeParseException => Left(s"Invalid date format: $str")

object InvoiceDueDate extends Newtype[Option[LocalDate]]

type InvoiceAmount = InvoiceAmount.Type
type InvoiceBuyer = InvoiceBuyer.Type
type InvoiceSeller = InvoiceSeller.Type
type InvoiceDate = InvoiceDate.Type
type InvoiceDueDate = InvoiceDueDate.Type

// Define the Invoice case class
case class Invoice(
  id: InvoiceId,
  organization: Organization,
  amount: InvoiceAmount,
  buyer: InvoiceBuyer,
  seller: InvoiceSeller,
  date: InvoiceDate,
  dueDate: InvoiceDueDate,
)
final case class CreateInvoice(
  amount: InvoiceAmount,
  buyer: InvoiceBuyer,
  seller: InvoiceSeller,
  date: InvoiceDate,
  dueDate: InvoiceDueDate,
)
// Custom Eq and Show instances for Money and Currency
object Invoice:
  given Eq[Currency] =
    Eq.and(Eq.and(Eq.by(_.code), Eq.by(_.symbol)), Eq.by(_.name))

  given Eq[Money] =
    Eq.and(Eq.by(_.amount), Eq.by(_.currency))

  given Show[Money] =
    Show.fromToString

  given Show[Currency] =
    Show.fromToString

  given Eq[Invoice] =
    Eq.fromUniversalEquals

  given Show[Invoice] =
    Show.fromToString
  def fromCreateInvoice(
    id: InvoiceId,
    organization: Organization,
    createInvoice: CreateInvoice,
  ): Invoice =
    Invoice(
      id = id,
      organization = organization,
      amount = createInvoice.amount,
      buyer = createInvoice.buyer,
      seller = createInvoice.seller,
      date = createInvoice.date,
      dueDate = createInvoice.dueDate,
    )
