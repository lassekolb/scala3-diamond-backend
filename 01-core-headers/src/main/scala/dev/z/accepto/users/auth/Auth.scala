package dev.z
package accepto
package users
package auth

type EncryptedPassword = EncryptedPassword.Type
object EncryptedPassword extends Newtype[String]:
  given Eq[EncryptedPassword] =
    Eq.by(_.unwrap)

type Password = Password.Type
object Password extends Newtype[String]

type PasswordSalt = PasswordSalt.Type
object PasswordSalt extends Newtype[NonEmptyString]

type JwtAccessTokenKeyConfig = JwtAccessTokenKeyConfig.Type
object JwtAccessTokenKeyConfig extends Newtype[NonEmptyString]

type UserRepr = UserRepr.Type
object UserRepr extends Subtype[String]
