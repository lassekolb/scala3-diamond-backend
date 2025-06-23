package dev.z
package accepto
package invoices

import scala.util.control.NoStackTrace

sealed trait CreateInvoiceError extends NoStackTrace

object CreateInvoiceError:
  final case class OrganizationDoesNotExist(organizationId: OrganizationId)
      extends CreateInvoiceError:
    override def getMessage: String =
      s"Organization with id $organizationId does not exist."

  final case class UserNotAuthorized(userId: UserId, organizationId: OrganizationId)
      extends CreateInvoiceError:
    override def getMessage: String =
      s"User $userId is not authorized for organization $organizationId."

  final case class InvoiceAlreadyExists(invoiceId: InvoiceId, organizationId: OrganizationId)
      extends CreateInvoiceError:
    override def getMessage: String =
      s"Invoice with id $invoiceId already exists for organization $organizationId."
