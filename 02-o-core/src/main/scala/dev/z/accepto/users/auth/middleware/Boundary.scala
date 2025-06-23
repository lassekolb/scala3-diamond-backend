package dev.z
package accepto
package users
package auth
package middleware

import cats.*
import cats.syntax.all.*

object BoundaryImpl:
  def make[F[_]: Monad, Authy, Token](
    dependencies: Dependencies[F, Authy, Token]
  ): Boundary[F, CommonUserSession, Authy, Token] =
    new Boundary[F, CommonUserSession, Authy, Token]:
      override lazy val authMiddleware: F[AuthMiddleware[F, CommonUserSession, Authy, Token]] =
        dependencies.config.map(_.tokenKey).flatMap(dependencies.auth).map { auth =>
          AuthMiddleware(
            auth,
            find =
              token =>
                dependencies
                  .getUserSessionFromCache(token)
                  .nested
                  .map {
                    case session: UserSessionWithOrganization =>
                      Session(CommonUser(session.user), Some(session.organization))
                    case session: UserSession =>
                      Session(CommonUser(session.user), None)
                  }
                  .value,
          )
        }

  trait Dependencies[F[_], Authy, Token]
      extends HasConfig[F, Config]
         with Redis[F, Token]
         with Auth[F, Authy]

  def make[F[_]: Monad, Authy, Token](
    hasConfig: HasConfig[F, Config],
    redis: Redis[F, Token],
    _auth: Auth[F, Authy],
  ): Boundary[F, CommonUserSession, Authy, Token] =
    make:
      new Dependencies[F, Authy, Token]:
        override def config: F[Config] =
          hasConfig.config

        override def getUserSessionFromCache(token: Token): F[Option[UserSession]] =
          redis.getUserSessionFromCache(token)

        override def auth(tokenKey: JwtAccessTokenKeyConfig): F[Authy] =
          _auth.auth(tokenKey)
