package dev.z
package accepto

import cats.*
import cats.effect.*
import cats.syntax.all.*
import dev.profunktor.redis4cats.effect.*
import fs2.io.net.Network
import natchez.Trace
import org.typelevel.log4cats.*

object ResourcesLoader:
  def load[F[_]: MkRedis: Logger: Async: Trace: Network: std.Console: NonEmptyParallel](
    appEnvironment: AppEnvironment
  ): F[Resource[F, Resources[F]]] =
    (
      RedisSessionLoader.load(appEnvironment),
      PostgresSessionLoader.load,
      // HttpClientLoader.load(appEnvironment),
    ).parTupled.map(_.parMapN(Resources.apply))
