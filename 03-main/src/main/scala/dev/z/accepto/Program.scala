package dev.z
package accepto

import cats.*
import cats.effect.*
import cats.effect.std.Supervisor
import cats.syntax.all.*
import dev.profunktor.redis4cats.log4cats.*
import fs2.io.net.Network
import natchez.Trace
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Program:
  def make[F[_]: NonEmptyParallel: Trace: Async: Network: std.Console]: F[Nothing] =
    given logger: Logger[F] = Slf4jLogger.getLogger

    AppEnvironmentConfigLoader
      .load
      .flatMap(ResourcesLoader.load[F])
      .flatMap[Nothing](runHttpServerForeverUnderSupervision)

  private def runHttpServerForeverUnderSupervision[F[_]: NonEmptyParallel: Async: Network: Logger](
    resources: Resource[F, Resources[F]]
  ): F[Nothing] =
    Supervisor[F].use[Nothing] { supervisor =>
      given Background[F] = BackgroundImpl.make(supervisor)

      resources
        .evalMap(makeHttpServer[F])
        .flatMap(_.serve)
        .useForever
    }

  private def makeHttpServer[F[_]: NonEmptyParallel: Async: Network: Logger: Background](
    resources: Resources[F]
  ): F[HttpServer[F]] =
    for
      httpServerConfig <- HttpServerConfigLoader.load
      otherDependencies <-
        (
          RetryPolicyLoader.load,
          JwtExpire.make,
          users.auth.middleware.admin.DI.make.middleware,
          users.auth.middleware.DI.make(resources.redis).middleware,
        ).parTupled
      controllers <- Function.tupled(Controllers.make(resources))(otherDependencies)
    yield HttpServer.make(httpServerConfig, HttpApp.make(controllers))
