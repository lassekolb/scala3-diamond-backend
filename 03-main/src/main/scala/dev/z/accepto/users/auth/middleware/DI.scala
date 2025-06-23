package dev.z
package accepto
package users
package auth
package middleware

import cats.*
import cats.effect.*
import dev.profunktor.auth.jwt
import dev.profunktor.redis4cats.RedisCommands

object DI:
  def make[F[_]: Async](
    redis: RedisCommands[F, String, String]
  ): Middleware[F, CommonUserSession] =
    MiddlewareImpl.make(
      boundary =
        BoundaryImpl.make(
          hasConfig = HasConfigImpl.make,
          redis = RedisImpl.make(redis),
          _auth = AuthImpl.make,
        )
    )

  private given Show[jwt.JwtToken] =
    // Show.fromToString
    Show(_.value)
