package dev.z
package accepto

import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import fs2.io.net.Network
import natchez.Trace
import skunk.*

object PostgresSession:
  def make[F[_]: Temporal: Network: Console: CheckPostgresConnection: Trace](
    c: PostgreSQLConfig
  ): SessionPool[F] =
    Session
      .pooled[F](
        host = c.host,
        port = c.port,
        user = c.user,
        password = c.password.some,
        database = c.database,
        max = c.max,
        debug = c.debug,
      )
      .evalTap(CheckPostgresConnection[F].checkPostgresConnection)
