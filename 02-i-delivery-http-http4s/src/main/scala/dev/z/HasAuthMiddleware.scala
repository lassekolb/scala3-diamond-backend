package dev.z

import org.http4s.server.*

trait HasAuthMiddleware[F[_], T]:
  def authMiddleware: AuthMiddleware[F, T]
