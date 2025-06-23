package dev.z
package accepto
package users
package auth
package middleware

import cats.effect.*
import ciris.*
import neotype.interop.ciris.given

object HasConfigImpl:
  def make[F[_]: Async]: HasConfig[F, Config] =
    new HasConfig[F, Config]:
      override def config: F[Config] =
        env("ACCEPTO_ACCESS_TOKEN_SECRET_KEY")
          .as[JwtAccessTokenKeyConfig]
          .map(Config.apply)
          .load[F]

  given ConfigDecoder[String, JwtAccessTokenKeyConfig] =
    ConfigDecoder[String, NonEmptyString].map(JwtAccessTokenKeyConfig.apply)
