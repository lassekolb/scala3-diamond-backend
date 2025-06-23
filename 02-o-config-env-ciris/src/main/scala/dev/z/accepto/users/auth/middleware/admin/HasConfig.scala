package dev.z
package accepto
package users
package auth
package middleware
package admin

import cats.effect.*
import cats.syntax.all.*
import ciris.*
import neotype.interop.ciris.given

object HasConfigImpl:
  def make[F[_]: Async]: HasConfig[F, Config] =
    new HasConfig[F, Config]:
      override def config: F[Config] =
        (
          env("ACCEPTO_JWT_SECRET_KEY").as[JwtSecretKeyConfig],
          env("ACCEPTO_ADMIN_USER_TOKEN").as[AdminUserTokenConfig],
        ).parMapN(Config.apply).load[F]

  given ConfigDecoder[String, JwtSecretKeyConfig] =
    ConfigDecoder[String, NonEmptyString].map(JwtSecretKeyConfig.apply)

  given ConfigDecoder[String, AdminUserTokenConfig] =
    ConfigDecoder[String, NonEmptyString].map(AdminUserTokenConfig.apply)
