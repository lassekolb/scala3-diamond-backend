package dev.z
package accepto
package users
package auth
package middleware

trait Redis[F[_], Token]:
  def getUserSessionFromCache(token: Token): F[Option[UserSession]]

trait Auth[F[_], Authy]:
  def auth(tokenKey: JwtAccessTokenKeyConfig): F[Authy]
