package dev.z

import cats.effect.Sync
import cats.syntax.all.*
import pdi.jwt.JwtClaim

trait JwtExpire[F[_]]:
  def expiresIn(claim: JwtClaim, exp: TokenExpiration): F[JwtClaim]

object JwtExpire:
  def make[F[_]: Sync]: F[JwtExpire[F]] =
    JwtClock[F].utc.map { implicit jClock =>
      new JwtExpire[F]:
        def expiresIn(claim: JwtClaim, exp: TokenExpiration): F[JwtClaim] =
          Sync[F].delay(claim.issuedNow.expiresIn(exp.unwrap.toSeconds))
    }

  def apply[F[_]: JwtExpire]: JwtExpire[F] =
    implicitly
