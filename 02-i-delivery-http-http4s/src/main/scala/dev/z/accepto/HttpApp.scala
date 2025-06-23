package dev.z
package accepto

import scala.concurrent.duration.*

import cats.data.*
import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.server.Router
import org.http4s.server.middleware.*

object HttpApp:
  def make[F[_]: Async](controllers: List[Controller[F]]): HttpApp[F] =
    // Collect common (public) and admin routes from controllers.
    val (commonRoutes, adminRoutes) =
      controllers
        .foldLeft((Vector.empty[HttpRoutes[F]], Vector.empty[HttpRoutes[F]])):
          case ((common, admin), c: Controller.Common[F]) =>
            common.appended(c.routes) -> admin
          case ((common, admin), c: Controller.Admin[F]) =>
            common -> admin.appended(c.routes)
        .bimap(_.reduceLeft(_ `combineK` _), _.reduceLeftOption(_ `combineK` _))

    lazy val routes: HttpRoutes[F] =
      Router(
        version.v1 -> commonRoutes
      )

    val corsPolicy =
      CORS
        .policy
        .withAllowOriginHost(_ => true)
        .withAllowCredentials(false)
        .withMaxAge(1.day)
        .withAllowMethodsAll
        .withAllowHeadersAll

    lazy val middleware: HttpRoutes[F] => HttpRoutes[F] =
      List[HttpRoutes[F] => HttpRoutes[F]](
        AutoSlash(_),
        routes => corsPolicy(routes),
        Timeout(60.seconds)(_),
      ).reduceLeft(_ andThen _)

    lazy val loggers: HttpApp[F] => HttpApp[F] =
      List[HttpApp[F] => HttpApp[F]](
        RequestLogger.httpApp(true, true),
        ResponseLogger.httpApp(true, true),
      ).reduceLeft(_ andThen _)

    loggers(middleware(routes).orNotFound)
