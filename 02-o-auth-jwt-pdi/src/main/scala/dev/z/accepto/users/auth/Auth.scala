package dev.z
package accepto
package users
package auth

import cats.*
import cats.syntax.all.*
import dev.profunktor.*
import io.circe.*
import io.circe.syntax.*
// import java.time.Instant

object AuthImpl:
  def make[F[_]: GenUUID: Monad](jwtExpire: JwtExpire[F]): Auth[F, auth.jwt.JwtToken] =
    new Auth[F, auth.jwt.JwtToken]:
      override def createToken(config: Config): F[auth.jwt.JwtToken] =
        for
          uuid <- GenUUID[F].make
          claim <- expireClaim(uuid, config.tokenExpiration)
          secretKey = auth.jwt.JwtSecretKey(config.jwtAccessTokenKeyConfig.unwrap.getBytes)
          token <- auth.jwt.jwtEncode[F](claim, secretKey, pdi.jwt.JwtAlgorithm.HS256)
        yield token

      private def expireClaim(uuid: UUID, tokenExpiration: TokenExpiration): F[pdi.jwt.JwtClaim] =
        jwtExpire.expiresIn(
          pdi
            .jwt
            .JwtClaim(
              Json
                .obj(
                  "sub" -> uuid.toString.asJson
                )
                .noSpaces
            ),
          tokenExpiration,
        )
