package dev.z
package accepto
package users
package auth
package middleware
package admin

import cats.*
import cats.syntax.all.*

object BoundaryImpl:
  def make[F[_]: Monad: NonEmptyParallel, Authy, Token: Eq](
    dependencies: Dependencies[F, Authy, Token]
  ): Boundary[F, AdminUserSession, Authy, Token] =
    new Boundary[F, AdminUserSession, Authy, Token]:
      override lazy val authMiddleware: F[AuthMiddleware[F, AdminUserSession, Authy, Token]] =
        for
          config <- dependencies.config
          (adminToken, adminAuth) <- tokenAndAuth(config)
          claim <- dependencies.claim(adminToken, adminAuth)
        yield AuthMiddleware(
          adminAuth,
          find =
            token =>
              (token === adminToken)
                .guard[Option]
                .as(adminUserSession(claim))
                .pure,
        )

      private def tokenAndAuth(config: Config): F[(Token, Authy)] =
        (dependencies.token(config.adminKey), dependencies.auth(config.tokenKey)).parTupled

      private def adminUserSession(content: ClaimContent): AdminUserSession =
        AdminUserSession(AdminUser(User(UserId(content), UserName(NonEmptyString("admin")))))

  trait Dependencies[F[_], Authy, Token] extends HasConfig[F, Config] with Auth[F, Authy, Token]

  def make[F[_]: Monad: NonEmptyParallel, Authy, Token: Eq](
    hasConfig: HasConfig[F, Config],
    _auth: Auth[F, Authy, Token],
  ): Boundary[F, AdminUserSession, Authy, Token] =
    make:
      new Dependencies[F, Authy, Token]:
        override def config: F[Config] =
          hasConfig.config

        override def token(adminKey: AdminUserTokenConfig): F[Token] =
          _auth.token(adminKey)

        override def auth(tokenKey: JwtSecretKeyConfig): F[Authy] =
          _auth.auth(tokenKey)

        override def claim(token: Token, auth: Authy): F[ClaimContent] =
          _auth.claim(token, auth)
