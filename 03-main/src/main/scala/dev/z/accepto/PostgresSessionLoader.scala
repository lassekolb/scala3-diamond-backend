package dev.z
package accepto

import cats.effect.*
import cats.syntax.all.*
import ciris.*
import fs2.io.net.Network
import natchez.Trace
import neotype.interop.ciris.given
import org.typelevel.log4cats
import skunk.SessionPool

object PostgresSessionLoader:
  def load[F[_]: Async: Network: log4cats.Logger: std.Console: Trace]: F[SessionPool[F]] =
    implicit val logger: Logger[F] = LoggerImpl.make("PostgresSessionLoader")
    implicit val checkConnection: CheckPostgresConnection[F] = CheckPostgresConnection.make[F]

    loadConfig.map(PostgresSession.make[F])

  def loadConfig[F[_]: Async]: F[PostgreSQLConfig] =
    env("ACCEPTO_POSTGRES_PASSWORD")
      .as[NonEmptyString]
      .map { password =>
        PostgreSQLConfig(
          host = NonEmptyString("localhost"),
          port = UserPortNumber(5432),
          user = NonEmptyString("postgres"),
          password = password,
          database = NonEmptyString("accepto"),
          max = PosInt(10),
          debug = true,
        )
      }
      .load[F]
