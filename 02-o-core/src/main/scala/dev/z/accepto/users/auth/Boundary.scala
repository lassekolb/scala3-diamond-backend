package dev.z
package accepto
package users
package auth

import cats.*
import cats.syntax.all.*

object BoundaryImpl:
  def make[F[_]: MonadThrow, Token](dependencies: Dependencies[F, Token]): Boundary[F, Token] =
    new Boundary[F, Token]:
      override def newUser(userName: UserName, password: Password): F[Token] =
        dependencies
          .findUser(userName)
          .flatMap:
            case Some(_) => UserNameInUse(userName).raiseError[F, Token]
            case None => create(userName, password)

      private def create(userName: UserName, password: Password): F[Token] =
        for
          config <- dependencies.config
          userId <- createOrRaiseIfInUse(userName, password)
          token <- dependencies.createToken(config)
          user = CommonUser(User(userId, userName))
          userSession = Session.WithoutOrg(user)
          _ <- dependencies.cacheUserSessionInRedis(userSession, config.tokenExpiration)(token)
        yield token

      private def createOrRaiseIfInUse(userName: UserName, password: Password): F[UserId] =
        for
          encryptedPassword <- dependencies.encrypt(password)
          result <- dependencies.createUser(userName, encryptedPassword)
          userId <- result.fold(_.raiseError[F, UserId], _.pure[F])
        yield userId

      override def login(userName: UserName, password: Password): F[Token] =
        for
          config <- dependencies.config
          token <-
            dependencies
              .findUser(userName)
              .flatMap:
                case None => UserNotFound(userName).raiseError[F, Token]
                case Some(user) =>
                  dependencies
                    .verifyPassword(password, user.password)
                    .flatMap:
                      case false => InvalidPassword(user.name).raiseError[F, Token]
                      case true => getOrElseCreateToken(user.user, config)
        yield token

      private def getOrElseCreateToken(user: User, config: Config): F[Token] =
        dependencies
          .getTokenFromRedis(user.id)
          .flatMap:
            case Some(t) => t.pure[F]
            case None =>
              dependencies
                .createToken(config)
                .flatTap { token =>
                  val session = Session.WithoutOrg(user)
                  dependencies.cacheUserSessionInRedis(session, config.tokenExpiration)(token)
                }

      override def logout(token: Token, userId: UserId): F[Unit] =
        dependencies.deleteUserSessionInRedis(userId, token)

      override def logout1(session: UserSession): F[Unit] =
        dependencies.deleteUserSessionInRedis1(session)

      override def setOrg(session: UserSession, orgId: OrganizationId): F[Unit] =
        dependencies
          .getMembership(session, orgId)
          .flatMap:
            case Some(memb) =>
              val updatedSession = Session.WithOrg(session.user, memb.organization)
              dependencies.config.flatMap { config =>
                dependencies
                  .updateUserSessionInRedis(updatedSession, config.tokenExpiration)
                  .flatMap:
                    case Right(()) => ().pure[F]
                    case Left(error) =>
                      SessionError
                        .SessionUpdateFailed(
                          session.userId,
                          s"Failed to update session: $error",
                        )
                        .raiseError[F, Unit]
              }
            case None =>
              UserNotAMember(session.user.name).raiseError[F, Unit]

  trait Dependencies[F[_], Token]
      extends HasConfig[F, Config]
         with Persistence[F]
         with Crypto[F]
         with Auth[F, Token]
         with Redis[F, Token]

  def make[F[_]: MonadThrow, Token](
    hasConfig: HasConfig[F, Config],
    persistence: Persistence[F],
    crypto: Crypto[F],
    auth: Auth[F, Token],
    redis: Redis[F, Token],
  ): Boundary[F, Token] =
    make:
      new Dependencies[F, Token]:
        override def config: F[Config] =
          hasConfig.config
        override def findUser(userName: UserName): F[Option[UserWithPassword]] =
          persistence.findUser(userName)
        override def createUser(
          userName: UserName,
          password: EncryptedPassword,
        ): F[Either[UserNameInUse, UserId]] =
          persistence.createUser(userName, password)
        override def encrypt(password: Password): F[EncryptedPassword] =
          crypto.encrypt(password)
        override def verifyPassword(provided: Password, stored: EncryptedPassword): F[Boolean] =
          crypto.verifyPassword(provided, stored)
        override def createToken(config: Config): F[Token] =
          auth.createToken(config)
        override def cacheUserSessionInRedis(
          userSession: UserSession,
          expiresIn: TokenExpiration,
        )(
          token: Token
        ): F[Unit] =
          redis.cacheUserSessionInRedis(userSession, expiresIn)(token)
        override def updateUserSessionInRedis(
          userSession: UserSession,
          expiresIn: TokenExpiration,
        ): F[Either[SessionError[Token], Unit]] =
          redis.updateUserSessionInRedis(userSession, expiresIn)
        override def getTokenFromRedis(userId: UserId): F[Option[Token]] =
          redis.getTokenFromRedis(userId)
        override def deleteUserSessionInRedis(userId: UserId, token: Token): F[Unit] =
          redis.deleteUserSessionInRedis(userId, token)
        override def deleteUserSessionInRedis1(userSession: UserSession): F[Unit] =
          redis.deleteUserSessionInRedis1(userSession)
        override def checkMembership(userSession: UserSession, orgId: OrganizationId): F[Boolean] =
          persistence.checkMembership(userSession, orgId)
        override def getMembership(
          userSession: UserSession,
          orgId: OrganizationId,
        ): F[Option[Membership]] =
          persistence.getMembership(userSession, orgId)
