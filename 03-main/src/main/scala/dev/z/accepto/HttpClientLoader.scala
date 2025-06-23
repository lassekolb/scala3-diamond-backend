// package dev.z
// package accepto
//
// import scala.concurrent.duration.*
// import scala.util.chaining.*
//
// import cats.*
// import cats.effect.*
// import cats.syntax.all.*
// import org.http4s.client.Client
// import org.http4s.ember.client.EmberClientBuilder
//
// object HttpClientLoader:
//   def load[F[_]: Async: NonEmptyParallel](
//     appEnvironment: AppEnvironment
//   ): F[Resource[F, HttpClientResources[F]]] =
//     (
//       buildClient,
//       paymentConfig[F](appEnvironment).pure,
//     ).parTupled.map(_.mapN(HttpClientResources.apply))
//
//   private def buildClient[F[_]: Async]: F[Resource[F, Client[F]]] =
//     config.map { c =>
//       EmberClientBuilder
//         .default
//         .withTimeout(c.timeout)
//         .withIdleTimeInPool(c.idleTimeInPool)
//         .build
//     }
//
//   private def config[F[_]: Async]: F[Config] =
//     Config(
//       timeout = 60.seconds,
//       idleTimeInPool = 30.seconds,
//     ).pure
//
//   final private case class Config(
//     timeout: FiniteDuration,
//     idleTimeInPool: FiniteDuration,
//   )
//
//   private def paymentConfig[F[_]](
//     appEnvironment: AppEnvironment
//   ): Resource[F, PaymentConfig] =
//     appEnvironment
//       .pipe(Resource.pure)
//
// final case class HttpClientResources[F[_]](
//   client: Client[F]
//   // config: PaymentConfig,
// )
