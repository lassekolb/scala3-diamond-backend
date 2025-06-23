package dev.z

import java.util.UUID

import cats.implicits.*

object vars:
  protected class UUIDVar[A](f: UUID => A):
    def unapply(str: String): Option[A] =
      Either.catchNonFatal(f(UUID.fromString(str))).toOption

  object InvoiceId extends UUIDVar(accepto.invoices.InvoiceId.apply)
  object OrganizationId extends UUIDVar(accepto.OrganizationId.apply)
