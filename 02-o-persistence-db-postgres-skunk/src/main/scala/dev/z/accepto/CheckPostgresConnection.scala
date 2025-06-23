package dev.z
package accepto

import cats.effect.*
import cats.syntax.all.*
import skunk.*
import skunk.codec.text.*
import skunk.implicits.*

trait CheckPostgresConnection[F[_]]:
  def checkPostgresConnection(postgres: Resource[F, Session[F]]): F[Unit]

object CheckPostgresConnection:
  def make[F[_]: MonadCancelThrow: Logger]: CheckPostgresConnection[F] =
    new CheckPostgresConnection[F]:
      override def checkPostgresConnection(
        postgres: Resource[F, Session[F]]
      ): F[Unit] =
        postgres.use { session =>
          session.unique(sql"select version();".query(text)).flatMap { v =>
            Logger[F].info(s"Connected to Postgres $v")
          }
        }

  def apply[F[_]: CheckPostgresConnection]: CheckPostgresConnection[F] =
    implicitly
