package dev.z
package accepto
package users
package auth
package middleware

trait Boundary[F[_], A, Authy, Token]:
  def authMiddleware: F[AuthMiddleware[F, A, Authy, Token]]
