package dev.z
package accepto
package users
package auth

trait Persistence[F[_]]:
  def findUser(userName: UserName): F[Option[UserWithPassword]]
  def createUser(userName: UserName, password: EncryptedPassword): F[Either[UserNameInUse, UserId]]
  def checkMembership(userSession: UserSession, orgId: OrganizationId): F[Boolean]
  def getMembership(userSession: UserSession, orgId: OrganizationId): F[Option[Membership]]

trait Crypto[F[_]]:
  def encrypt(password: Password): F[EncryptedPassword]
  def verifyPassword(provided: Password, stored: EncryptedPassword): F[Boolean]

trait Auth[F[_], Token]:
  def createToken(config: Config): F[Token]

trait Redis[F[_], Token]:
  def cacheUserSessionInRedis(
    userSession: UserSession,
    expiresIn: TokenExpiration,
  )(
    token: Token
  ): F[Unit]
  def updateUserSessionInRedis(
    userSession: UserSession,
    expiresIn: TokenExpiration,
  ): F[Either[SessionError[Token], Unit]]
  def getTokenFromRedis(userId: UserId): F[Option[Token]]
  def deleteUserSessionInRedis(userId: UserId, token: Token): F[Unit]
  def deleteUserSessionInRedis1(session: UserSession): F[Unit]
