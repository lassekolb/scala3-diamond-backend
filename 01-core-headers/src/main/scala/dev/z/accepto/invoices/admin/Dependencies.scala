package dev.z
package accepto
package invoices
package admin

trait Persistence[F[_]]:
  def createInvoice(
    organization: Organization,
    createInvoice: CreateInvoice,
  ): F[Either[CreateInvoiceError, InvoiceId]]
  def createInvoices(
    organization: Organization,
    createInvoices: List[CreateInvoice],
  ): F[Either[CreateInvoiceError, List[InvoiceId]]]
  def createInvoicesStream(
    organization: Organization,
    createInvoice: Stream[F, CreateInvoice],
    batchSize: Int = 100,
  ): Stream[F, Either[CreateInvoiceError, List[InvoiceId]]]
