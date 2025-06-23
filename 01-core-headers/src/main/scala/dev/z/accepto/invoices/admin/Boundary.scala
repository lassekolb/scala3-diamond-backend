package dev.z
package accepto
package invoices
package admin

trait Boundary[F[_]]:
  def createInvoice(
    session: Session.WithOrg[AdminUser],
    createInvoice: CreateInvoice,
  ): F[InvoiceId]

  def createInvoices(
    session: Session.WithOrg[AdminUser],
    createInvoices: List[CreateInvoice],
  ): F[List[InvoiceId]]

  def createInvoicesStream(
    session: Session.WithOrg[AdminUser],
    createInvoice: Stream[F, CreateInvoice],
    batchSize: Int = 100,
  ): Stream[F, List[InvoiceId]]
