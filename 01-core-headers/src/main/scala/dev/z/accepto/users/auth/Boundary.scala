package dev.z
package accepto
package users
package auth

trait Boundary[F[_], Token]:
  def newUser(userName: UserName, password: Password): F[Token]
  def login(userName: UserName, password: Password): F[Token]
  def logout(token: Token, userId: UserId): F[Unit]
  def logout1(session: UserSession): F[Unit]
  def setOrg(session: UserSession, orgId: OrganizationId): F[Unit]
