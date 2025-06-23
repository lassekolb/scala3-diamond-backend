package dev.z
package accepto
package users
package auth
package middleware
package admin

import cats.*
import cats.syntax.all.*
import dev.profunktor.auth.*
import org.http4s.server.AuthMiddleware
import pdi.jwt.JwtClaim

object MiddlewareImpl:
  def make[F[_]: MonadThrow](
    boundary: Boundary[F, AdminUserSession, jwt.JwtAuth, jwt.JwtToken]
  ): Middleware[F, AdminUserSession] =
    new Middleware[F, AdminUserSession]:
      override lazy val middleware: F[AuthMiddleware[F, AdminUserSession]] =
        boundary.authMiddleware.map { m =>
          JwtAuthMiddleware(
            jwtAuth = m.auth,
            authenticate = adapt(m.find),
          )
        }

      private def adapt[A](
        in: jwt.JwtToken => F[Option[A]]
      ): jwt.JwtToken => JwtClaim => F[Option[A]] =
        profunktorJwtToken => _ => in(profunktorJwtToken)
