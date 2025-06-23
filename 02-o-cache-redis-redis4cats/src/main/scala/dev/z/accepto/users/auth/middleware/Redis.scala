package dev.z
package accepto
package users
package auth
package middleware

import cats.*
import cats.syntax.all.*
import dev.profunktor.redis4cats.RedisCommands
import io.circe.parser.decode

import dev.z.accepto.users.auth.CirceCodecs.given

object RedisImpl:
  def make[F[_]: Functor, Token: Show](
    redis: RedisCommands[F, String, String]
  ): Redis[F, Token] =
    new Redis[F, Token]:
      override def getUserSessionFromCache(token: Token): F[Option[UserSession]] =
        redis.get(token.show).map { optionOfString =>
          for
            string <- optionOfString
            userSession <- decode[UserSession](string).toOption
          yield userSession
        }
