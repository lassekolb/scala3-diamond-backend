package dev.z
package accepto
package users
package auth
package middleware
package admin

import cats.*
import cats.syntax.all.*
import dev.profunktor.auth.jwt
import io.circe.parser.decode

import dev.z.accepto.users.auth.middleware.admin.CirceCodecs.given

object AuthImpl:
  def make[F[_]: MonadThrow]: Auth[F, jwt.JwtAuth, jwt.JwtToken] =
    new Auth[F, jwt.JwtAuth, jwt.JwtToken]:
      override def token(adminKey: AdminUserTokenConfig): F[jwt.JwtToken] =
        jwt.JwtToken(adminKey.unwrap).pure

      override def auth(tokenKey: JwtSecretKeyConfig): F[jwt.JwtAuth] =
        jwt
          .JwtAuth
          .hmac(tokenKey.unwrap.toCharArray(), pdi.jwt.JwtAlgorithm.HS256)
          .pure
          .widen

      override def claim(token: jwt.JwtToken, auth: jwt.JwtAuth): F[ClaimContent] =
        for
          jwtClaim <- jwt.jwtDecode(token, auth)
          claimContent <- ApplicativeThrow[F].fromEither(decode(jwtClaim.content))
        yield claimContent
