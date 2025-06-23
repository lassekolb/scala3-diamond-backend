package dev.z
package accepto

import cats.effect.*
import fs2.io.net.Network
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.defaults.Banner
import org.typelevel.log4cats.Logger

trait HttpServer[F[_]]:
  def serve: Resource[F, Server]

object HttpServer:
  def make[F[_]: Async: Network: Logger](
    cfg: HttpServerConfig,
    httpApp: HttpApp[F],
  ): HttpServer[F] =
    new HttpServer[F]:
      lazy val serve: Resource[F, Server] =
        EmberServerBuilder
          .default[F]
          .withHost(cfg.host)
          .withPort(cfg.port)
          .withHttpApp(httpApp)
          .build
          .evalTap(showEmberBanner)

      private def showEmberBanner(s: Server): F[Unit] =
        Logger[F].info(s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}")

  def apply[F[_]: HttpServer]: HttpServer[F] =
    implicitly
