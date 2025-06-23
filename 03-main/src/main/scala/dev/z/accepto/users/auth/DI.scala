package dev.z
package accepto
package users
package auth

import cats.*
import cats.effect.*
import cats.syntax.all.*
import dev.profunktor.*
import dev.profunktor.redis4cats.RedisCommands
import org.http4s.server.AuthMiddleware
import org.typelevel.log4cats
import skunk.Session

object DI:
  def make[F[_]: Async: GenUUID: NonEmptyParallel: log4cats.Logger](
    postgres: Resource[F, Session[F]],
    redis: RedisCommands[F, String, String],
    authMiddleware: AuthMiddleware[F, CommonUserSession],
    jwtExpire: JwtExpire[F],
  ): F[Controller[F]] =
    // implicit val logger: Logger[F] = LoggerImpl.make("Persistence")
    for
      hasConfig <- HasConfigImpl.make.pure
      config <- hasConfig.config
      crypto <- CryptoImpl.make(config.passwordSalt)
    yield ControllerImpl.make(
      authMiddleware,
      boundary =
        BoundaryImpl.make(
          hasConfig = hasConfig,
          persistence = PersistenceImpl.make(postgres),
          crypto = crypto,
          auth = AuthImpl.make(jwtExpire),
          redis = RedisImpl.make(redis, stringToToken = auth.jwt.JwtToken.apply),
        ),
    )
  private given Show[auth.jwt.JwtToken] =
    Show(_.value)
