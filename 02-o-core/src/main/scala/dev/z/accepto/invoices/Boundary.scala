package dev.z
package accepto
package invoices

import cats.MonadThrow
import cats.syntax.all.*
import fs2.Stream

object BoundaryImpl:
  def make[F[_]: MonadThrow](dependencies: Dependencies[F]): Boundary[F] =
    new Boundary[F]:
      override def createInvoice(
        session: UserSessionWithOrganization,
        createInvoice: CreateInvoice,
      ): F[InvoiceId] =
        dependencies
          .createInvoice(session, createInvoice)
          .flatMap(_.fold(_.raiseError[F, InvoiceId], _.pure[F]))

      override def createInvoices(
        session: UserSessionWithOrganization,
        createInvoices: List[CreateInvoice],
      ): F[List[InvoiceId]] =
        dependencies
          .createInvoices(session, createInvoices)
          .flatMap(_.fold(_.raiseError[F, List[InvoiceId]], _.pure[F]))

      override def createInvoicesStream(
        session: UserSessionWithOrganization,
        createInvoice: Stream[F, CreateInvoice],
        batchSize: Int,
      ): Stream[F, InvoiceId] =
        dependencies
          .createInvoicesStream(session, createInvoice, batchSize)
          .flatMap:
            case Left(error) => Stream.raiseError[F](error)
            case Right(ids) => Stream.emits(ids)

      override def findInvoice(
        session: UserSessionWithOrganization,
        invoiceId: InvoiceId,
      ): F[Option[Invoice]] =
        dependencies.findInvoice(session, invoiceId)

      override def streamInvoicesByOrganization(
        session: UserSessionWithOrganization
      ): Stream[F, Invoice] =
        dependencies.streamInvoicesByOrganization(session)

      override def streamInvoicesByDate(
        session: UserSessionWithOrganization,
        startDate: InvoiceDate,
        endDate: InvoiceDate,
      ): Stream[F, Invoice] =
        dependencies.streamInvoicesByDate(session, startDate, endDate)

      override def streamInvoicesByIds(
        session: UserSessionWithOrganization,
        ids: List[InvoiceId],
      ): Stream[F, Invoice] =
        dependencies.streamInvoicesByIds(session, ids)

  trait Dependencies[F[_]] extends Persistence[F]

  def make[F[_]: MonadThrow](
    persistence: Persistence[F]
  ): Boundary[F] =
    make:
      new Dependencies[F]:

        override def createInvoice(
          session: UserSessionWithOrganization,
          createInvoice: CreateInvoice,
        ): F[Either[CreateInvoiceError, InvoiceId]] =
          persistence.createInvoice(session, createInvoice)

        override def createInvoices(
          session: UserSessionWithOrganization,
          createInvoices: List[CreateInvoice],
        ): F[Either[CreateInvoiceError, List[InvoiceId]]] =
          persistence.createInvoices(session, createInvoices)

        override def createInvoicesStream(
          session: UserSessionWithOrganization,
          createInvoice: Stream[F, CreateInvoice],
          batchSize: Int,
        ): Stream[F, Either[CreateInvoiceError, List[InvoiceId]]] =
          persistence.createInvoicesStream(session, createInvoice, batchSize)

        override def findInvoice(
          session: UserSessionWithOrganization,
          invoiceId: InvoiceId,
        ): F[Option[Invoice]] =
          persistence.findInvoice(session, invoiceId)

        override def streamInvoicesByOrganization(
          session: UserSessionWithOrganization
        ): Stream[F, Invoice] =
          persistence.streamInvoicesByOrganization(session)

        override def streamInvoicesByDate(
          session: UserSessionWithOrganization,
          startDate: InvoiceDate,
          endDate: InvoiceDate,
        ): Stream[F, Invoice] =
          persistence.streamInvoicesByDate(session, startDate, endDate)

        override def streamInvoicesByIds(
          session: UserSessionWithOrganization,
          ids: List[InvoiceId],
        ): Stream[F, Invoice] =
          persistence.streamInvoicesByIds(session, ids)
