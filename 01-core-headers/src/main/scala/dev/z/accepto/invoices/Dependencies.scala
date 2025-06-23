package dev.z
package accepto
package invoices

trait Persistence[F[_]]:
  def createInvoice(
    session: UserSessionWithOrganization,
    createInvoice: CreateInvoice,
  ): F[Either[CreateInvoiceError, InvoiceId]]
  def createInvoices(
    session: UserSessionWithOrganization,
    createInvoices: List[CreateInvoice],
  ): F[Either[CreateInvoiceError, List[InvoiceId]]]
  def createInvoicesStream(
    session: UserSessionWithOrganization,
    createInvoice: Stream[F, CreateInvoice],
    batchSize: Int = 100,
  ): Stream[F, Either[CreateInvoiceError, List[InvoiceId]]]
  def findInvoice(session: UserSessionWithOrganization, invoiceId: InvoiceId): F[Option[Invoice]]
  def streamInvoicesByOrganization(session: UserSessionWithOrganization): Stream[F, Invoice]
  def streamInvoicesByDate(
    session: UserSessionWithOrganization,
    startDate: InvoiceDate,
    endDate: InvoiceDate,
  ): Stream[F, Invoice]
  def streamInvoicesByIds(
    session: UserSessionWithOrganization,
    ids: List[InvoiceId],
  ): Stream[F, Invoice]
