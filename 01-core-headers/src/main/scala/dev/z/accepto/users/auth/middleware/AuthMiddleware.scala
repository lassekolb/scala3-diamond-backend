package dev.z
package accepto
package users
package auth
package middleware

final case class AuthMiddleware[F[_], A, Authy, Token](
  auth: Authy,
  find: Token => F[Option[A]],
)
