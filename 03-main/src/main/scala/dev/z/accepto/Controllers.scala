package dev.z
package accepto

import cats.*
import cats.effect.*
import cats.syntax.all.*
import org.http4s.server.AuthMiddleware
import org.typelevel.log4cats.*
import retry.RetryPolicy

object Controllers:
  def make[F[_]: NonEmptyParallel: Async: Logger: Background](
    resources: Resources[F]
  )(
    policy: RetryPolicy[F, Throwable],
    jwtExpire: JwtExpire[F],
    adminAuth: AuthMiddleware[F, AdminUserSession],
    userAuth: AuthMiddleware[F, CommonUserSession],
  ): F[List[Controller[F]]] =
    import resources.*

    for controllers <-
          List(
            users
              .auth
              .DI
              .make(postgres, redis, userAuth, jwtExpire),
            organizations.DI.make(postgres, userAuth),
            invoices.DI.make(postgres, userAuth),
          ).sequence
    yield controllers
