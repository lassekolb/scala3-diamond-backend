package dev.z
package accepto

type UserId = UserId.Type
object UserId extends Newtype[UUID]:
  given Show[UserId] =
    Show.show(_.toString)

object UserName extends Newtype[NonEmptyString]
type UserName = UserName.Type

// Define User case class
case class User(
  id: UserId,
  name: UserName,
)

object User:
  given Eq[User] =
    Eq.fromUniversalEquals
  given Show[User] =
    Show.fromToString

type CommonUser = CommonUser.Type
object CommonUser extends Subtype[User]:
  given Eq[CommonUser] =
    Eq.fromUniversalEquals
  given Show[CommonUser] =
    Show.show(_.toString)

type AdminUser = AdminUser.Type
object AdminUser extends Subtype[User]:
  given Eq[AdminUser] =
    Eq.fromUniversalEquals
  given Show[AdminUser] =
    Show.show(_.toString)
