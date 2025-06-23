package dev.z

import cats.MonadThrow
import cats.syntax.all.*
import io.circe.Codec
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

object neotypeIntegration:
  implicit class NeotypeRequestDecoder[F[_]: JsonDecoder: MonadThrow](req: Request[F])
      extends Http4sDsl[F]:
    def decodeR[A: Codec](f: A => F[Response[F]]): F[Response[F]] =
      req
        .asJsonDecode[A]
        .attempt
        .flatMap:
          case Left(e) =>
            Option(e.getCause) match
              case Some(c) if c.getMessage.contains("Failed to create") =>
                BadRequest(c.getMessage)
              case _ =>
                UnprocessableEntity(s"Invalid request: ${e.getMessage}")
          case Right(a) => f(a)
