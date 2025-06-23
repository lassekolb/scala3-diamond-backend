package dev.z
package accepto
package organizations

import cats.effect.*
import cats.syntax.all.*
import org.http4s.circe.JsonDecoder
import org.http4s.server.AuthMiddleware
import skunk.Session

object DI:
  def make[F[_]: MonadCancelThrow: GenUUID: JsonDecoder](
    postgres: Resource[F, Session[F]],
    authMiddleware: AuthMiddleware[F, CommonUserSession],
  )(implicit
    C: fs2.Compiler[F, F]
  ): F[Controller[F]] =
    ControllerImpl
      .make(
        authMiddleware,
        boundary =
          BoundaryImpl.make(
            persistence = PersistenceImpl.make(postgres)
          ),
      )
      .pure
