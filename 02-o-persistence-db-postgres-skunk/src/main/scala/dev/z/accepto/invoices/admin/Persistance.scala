package dev.z
package accepto
package invoices
package admin

import cats.effect.*
import cats.syntax.all.*
import fs2.Stream
import skunk.*
import skunk.implicits.*

object PersistenceImpl:
  def make[F[_]: GenUUID: MonadCancelThrow](
    postgres: Resource[F, Session[F]]
  ): Persistence[F] =
    new Persistence[F]:
      override def createInvoice(
        organization: Organization,
        createInvoice: CreateInvoice,
      ): F[Either[CreateInvoiceError, InvoiceId]] =
        postgres.use { session =>
          GenUUID[F].make.map(InvoiceId(_)).flatMap { newInvoiceId =>
            val invoice = Invoice.fromCreateInvoice(newInvoiceId, organization, createInvoice)
            session.prepare(SQL.insertInvoice).flatMap { cmd =>
              cmd
                .execute(invoice)
                .as(Right(newInvoiceId))
                .recoverWith:
                  case SqlState.ForeignKeyViolation(_) =>
                    CreateInvoiceError
                      .OrganizationDoesNotExist(organization.id)
                      .asLeft[InvoiceId]
                      .pure[F]
            }
          }
        }
      override def createInvoices(
        organization: Organization,
        createInvoices: List[CreateInvoice],
      ): F[Either[CreateInvoiceError, List[InvoiceId]]] =
        postgres.use { s =>
          createInvoices.traverse(_ => GenUUID[F].make.map(InvoiceId(_))).flatMap { newInvoiceIds =>
            val invoices =
              newInvoiceIds
                .zip(createInvoices)
                .map:
                  case (id, ci) =>
                    Invoice.fromCreateInvoice(id, organization, ci)
            s.prepare(SQL.insertInvoices(invoices.length)).flatMap { cmd =>
              cmd
                .execute(invoices)
                .as(Right(newInvoiceIds))
                .recoverWith:
                  case SqlState.ForeignKeyViolation(_) =>
                    CreateInvoiceError
                      .OrganizationDoesNotExist(organization.id)
                      .asLeft[List[InvoiceId]]
                      .pure[F]
            }
          }
        }
      override def createInvoicesStream(
        organization: Organization,
        createInvoices: Stream[F, CreateInvoice],
        batchSize: Int = 100,
      ): Stream[F, Either[CreateInvoiceError, List[InvoiceId]]] =
        Stream.resource(postgres).flatMap { session =>
          createInvoices.chunkN(batchSize).evalMap { chunk =>
            val createList = chunk.toList
            createList.traverse(_ => GenUUID[F].make.map(InvoiceId(_))).flatMap { newInvoiceIds =>
              val invoices =
                newInvoiceIds
                  .zip(createList)
                  .map:
                    case (id, ci) =>
                      Invoice.fromCreateInvoice(id, organization, ci)
              session.prepare(SQL.insertInvoices(invoices.length)).flatMap { cmd =>
                cmd
                  .execute(invoices)
                  .as(Right(newInvoiceIds))
                  .recoverWith:
                    case SqlState.ForeignKeyViolation(_) =>
                      CreateInvoiceError
                        .OrganizationDoesNotExist(organization.id)
                        .asLeft[List[InvoiceId]]
                        .pure[F]
              }
            }
          }
        }
