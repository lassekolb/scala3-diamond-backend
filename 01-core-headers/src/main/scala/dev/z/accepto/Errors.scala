package dev.z
package accepto

import scala.util.control.NoStackTrace

enum OrganizationError extends NoStackTrace:
  case NoOrganizationSelected(userId: UserId)
