package dev.z
package accepto

object UserPortNumber extends Subtype[Int]:
  inline override def validate(value: Int): Boolean =
    value >= 1024 && value <= 49151

type UserPortNumber = UserPortNumber.Type

object PosInt extends Subtype[Int]:
  inline override def validate(value: Int): Boolean =
    value > 0

type PosInt = PosInt.Type

final case class PostgreSQLConfig(
  host: NonEmptyString,
  port: UserPortNumber,
  user: NonEmptyString,
  password: NonEmptyString,
  database: NonEmptyString,
  max: PosInt,
  debug: Boolean,
)
