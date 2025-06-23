package dev.z
package accepto
package users
package auth

import scala.concurrent.duration.*

import cats.effect.*
import cats.syntax.all.*
import ciris.*
import neotype.interop.ciris.given

object HasConfigImpl:
  def make[F[_]: Async]: HasConfig[F, Config] =
    new HasConfig[F, Config]:
      override def config: F[Config] =
        (
          ConfigValue.default(TokenExpiration(6.hours)),
          env("ACCEPTO_ACCESS_TOKEN_SECRET_KEY").as[JwtAccessTokenKeyConfig],
          env("ACCEPTO_PASSWORD_SALT").as[PasswordSalt],
        ).parMapN(Config.apply).load[F]

  given ConfigDecoder[String, JwtAccessTokenKeyConfig] =
    ConfigDecoder[String, NonEmptyString].map(JwtAccessTokenKeyConfig.apply)

  given ConfigDecoder[String, PasswordSalt] =
    ConfigDecoder[String, NonEmptyString].map(PasswordSalt.apply)
