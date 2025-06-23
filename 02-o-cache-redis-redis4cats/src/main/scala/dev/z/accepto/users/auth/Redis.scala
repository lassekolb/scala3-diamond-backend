package dev.z
package accepto
package users
package auth

import cats.*
import cats.syntax.all.*
import dev.profunktor.redis4cats.RedisCommands
import io.circe.syntax.*

import dev.z.accepto.users.auth.CirceCodecs.given

object RedisImpl:
  def make[F[_]: Monad: NonEmptyParallel, Token: Show](
    redis: RedisCommands[F, String, String],
    stringToToken: String => Token,
  ): Redis[F, Token] =
    new Redis[F, Token]:
      override def cacheUserSessionInRedis(
        userSession: UserSession,
        expiresIn: TokenExpiration,
      )(
        token: Token
      ): F[Unit] =
        (
          redis.setEx(token.show, userSession.asJson.noSpaces.show, expiresIn.unwrap),
          redis.setEx(userSession.userId.show, token.show, expiresIn.unwrap),
        ).parTupled.void

      override def updateUserSessionInRedis(
        userSession: UserSession,
        expiresIn: TokenExpiration,
      ): F[Either[SessionError[Token], Unit]] =
        getTokenFromRedis(userSession.userId).flatMap:
          case None =>
            (SessionError.TokenNotFound(userSession.userId): SessionError[Token])
              .asLeft[Unit]
              .pure[F]
          case Some(token) =>
            (
              redis.setEx(token.show, userSession.asJson.noSpaces.show, expiresIn.unwrap),
              redis.setEx(userSession.userId.show, token.show, expiresIn.unwrap),
            ).parTupled
              .void
              .map(_.asRight[SessionError[Token]])

      override def getTokenFromRedis(userId: UserId): F[Option[Token]] =
        redis.get(userId.show).nested.map(stringToToken).value

      override def deleteUserSessionInRedis(userId: UserId, token: Token): F[Unit] =
        (
          redis.del(token.show),
          redis.del(userId.show),
        ).parTupled.void
      override def deleteUserSessionInRedis1(userSession: UserSession): F[Unit] =
        getTokenFromRedis(userSession.userId).flatMap:
          case Some(token) =>
            (
              redis.del(token.show),
              redis.del(userSession.userId.show),
            ).parTupled.void
          case None =>
            ().pure[F]
