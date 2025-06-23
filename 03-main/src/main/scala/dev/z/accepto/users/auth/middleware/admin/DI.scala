package dev.z
package accepto
package users
package auth
package middleware
package admin

import cats.*
import cats.effect.*
import dev.profunktor.auth.jwt

object DI:
  def make[F[_]: Async: NonEmptyParallel]: Middleware[F, AdminUserSession] =
    MiddlewareImpl.make(
      boundary =
        BoundaryImpl.make(
          hasConfig = HasConfigImpl.make,
          _auth = AuthImpl.make,
        )
    )
  private given Eq[jwt.JwtToken] =
    Eq.fromUniversalEquals
