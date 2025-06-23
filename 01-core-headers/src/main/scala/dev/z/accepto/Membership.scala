package dev.z
package accepto

import cats.*
import cats.derived.*

sealed trait MembershipRole derives Eq, Show
object MembershipRole:
  case object Admin extends MembershipRole
  case object Member extends MembershipRole

final case class Membership(
  user: User,
  organization: Organization,
  role: MembershipRole,
)

object Membership:
  given Eq[Membership] =
    Eq.fromUniversalEquals
  given Show[Membership] =
    Show.fromToString
