package dev.z
package accepto
package invoices

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
      override def findInvoice(
        session: UserSessionWithOrganization,
        invoiceId: InvoiceId,
      ): F[Option[Invoice]] =
        postgres.use { s =>
          s.prepare(SQL.selectInvoice).flatMap { q =>
            q.option((session.orgId, session.userId, invoiceId))
          }
        }
      override def createInvoice(
        session: UserSessionWithOrganization,
        createInvoice: CreateInvoice,
      ): F[Either[CreateInvoiceError, InvoiceId]] =
        postgres.use { s =>
          GenUUID[F].make.map(InvoiceId(_)).flatMap { newInvoiceId =>
            s.prepare(SQL.insertInvoiceAsUser).flatMap { cmd =>
              cmd
                .execute((session.orgId, session.userId, newInvoiceId, createInvoice))
                .flatMap:
                  case completion if completion == skunk.data.Completion.Insert(0) =>
                    CreateInvoiceError
                      .UserNotAuthorized(session.userId, session.orgId)
                      .asLeft[InvoiceId]
                      .pure[F]
                  case _ =>
                    newInvoiceId.asRight[CreateInvoiceError].pure[F]
                .recoverWith:
                  case SqlState.ForeignKeyViolation(_) =>
                    CreateInvoiceError
                      .OrganizationDoesNotExist(session.orgId)
                      .asLeft[InvoiceId]
                      .pure[F]
            }
          }
        }
      override def createInvoices(
        session: UserSessionWithOrganization,
        createInvoices: List[CreateInvoice],
      ): F[Either[CreateInvoiceError, List[InvoiceId]]] =
        postgres.use { s =>
          createInvoices.traverse(_ => GenUUID[F].make.map(InvoiceId(_))).flatMap { newInvoiceIds =>
            val invoiceData = newInvoiceIds.zip(createInvoices)
            s.prepare(SQL.insertInvoicesAsUser(invoiceData.length)).flatMap { cmd =>
              cmd
                .execute((session.orgId, session.userId, invoiceData))
                .flatMap:
                  case comp if comp == skunk.data.Completion.Insert(0) =>
                    CreateInvoiceError
                      .UserNotAuthorized(session.userId, session.orgId)
                      .asLeft[List[InvoiceId]]
                      .pure[F]
                  case _ =>
                    newInvoiceIds.asRight[CreateInvoiceError].pure[F]
                .recoverWith:
                  case SqlState.ForeignKeyViolation(_) =>
                    CreateInvoiceError
                      .OrganizationDoesNotExist(session.orgId)
                      .asLeft[List[InvoiceId]]
                      .pure[F]
            }
          }
        }
      override def createInvoicesStream(
        session: UserSessionWithOrganization,
        createInvoices: Stream[F, CreateInvoice],
        batchSize: Int = 100,
      ): Stream[F, Either[CreateInvoiceError, List[InvoiceId]]] =
        Stream.resource(postgres).flatMap { s =>
          createInvoices.chunkN(batchSize).evalMap { chunk =>
            val createList = chunk.toList
            createList.traverse(_ => GenUUID[F].make.map(InvoiceId(_))).flatMap { newInvoiceIds =>
              val invoiceData = newInvoiceIds.zip(createList)
              s.prepare(SQL.insertInvoicesAsUser(invoiceData.length)).flatMap { cmd =>
                cmd
                  .execute((session.orgId, session.userId, invoiceData))
                  .flatMap:
                    case skunk.data.Completion.Insert(count) if count == invoiceData.length =>
                      Right(newInvoiceIds).pure[F]
                    case skunk.data.Completion.Insert(_) =>
                      CreateInvoiceError
                        .UserNotAuthorized(session.userId, session.orgId)
                        .asLeft[List[InvoiceId]]
                        .pure[F]
                    case _ =>
                      CreateInvoiceError
                        .UserNotAuthorized(session.userId, session.orgId)
                        .asLeft[List[InvoiceId]]
                        .pure[F]
                  .recoverWith:
                    case SqlState.ForeignKeyViolation(_) =>
                      CreateInvoiceError
                        .OrganizationDoesNotExist(session.orgId)
                        .asLeft[List[InvoiceId]]
                        .pure[F]
              }
            }
          }
        }
      override def streamInvoicesByOrganization(
        session: UserSessionWithOrganization
      ): Stream[F, Invoice] =
        Stream.resource(postgres).flatMap { s =>
          Stream.eval(s.prepare(SQL.selectInvoices)).flatMap { q =>
            q.stream((session.orgId, session.userId), 1024)
          }
        }

      override def streamInvoicesByDate(
        session: UserSessionWithOrganization,
        startDate: InvoiceDate,
        endDate: InvoiceDate,
      ): Stream[F, Invoice] =
        Stream.resource(postgres).flatMap { s =>
          Stream.eval(s.prepare(SQL.selectInvoicesByDate)).flatMap { q =>
            q.stream((session.orgId, session.userId, startDate, endDate), 1024)
          }
        }

      override def streamInvoicesByIds(
        session: UserSessionWithOrganization,
        ids: List[InvoiceId],
      ): Stream[F, Invoice] =
        Stream.resource(postgres).flatMap { s =>
          Stream.eval(s.prepare(SQL.selectInvoicesByIds(ids.size))).flatMap { q =>
            q.stream((session.orgId, session.userId, ids), 1024)
          }
        }
