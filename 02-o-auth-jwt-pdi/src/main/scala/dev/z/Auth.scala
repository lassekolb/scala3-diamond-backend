package dev.z
package accepto
package users
package auth
package middleware

import cats.*
import cats.syntax.all.*
import dev.profunktor.auth.jwt

object AuthImpl:
  def make[F[_]: Applicative]: Auth[F, jwt.JwtAuth] =
    new Auth[F, jwt.JwtAuth]:
      override def auth(tokenKey: JwtAccessTokenKeyConfig): F[jwt.JwtAuth] =
        jwt
          .JwtAuth
          .hmac(tokenKey.unwrap.toCharArray(), pdi.jwt.JwtAlgorithm.HS256)
          .pure
          .widen
