package dev.z
package accepto
package invoices

trait Boundary[F[_]]:
  def createInvoice(
    session: UserSessionWithOrganization,
    createInvoice: CreateInvoice,
  ): F[InvoiceId]
  def createInvoices(
    session: UserSessionWithOrganization,
    createInvoices: List[CreateInvoice],
  ): F[List[InvoiceId]]
  def createInvoicesStream(
    session: UserSessionWithOrganization,
    createInvoice: Stream[F, CreateInvoice],
    batchSize: Int = 100,
  ): Stream[F, InvoiceId]
  def findInvoice(
    session: UserSessionWithOrganization,
    invoiceId: InvoiceId,
  ): F[Option[Invoice]]
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
