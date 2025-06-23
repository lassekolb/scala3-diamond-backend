package dev.z

import java.util.UUID

import cats.*
import cats.syntax.all.*

trait GenUUID[F[_]]:
  def make: F[UUID]
  def read(str: String): F[UUID]

object GenUUID:
  def apply[F[_]: GenUUID]: GenUUID[F] =
    implicitly

  implicit def forDefer[F[_]: Defer: ApplicativeThrow]: GenUUID[F] =
    new GenUUID[F]:
      def make: F[UUID] =
        Defer[F].defer(UUID.randomUUID().pure)

      def read(str: String): F[UUID] =
        ApplicativeThrow[F].catchNonFatal(UUID.fromString(str))
