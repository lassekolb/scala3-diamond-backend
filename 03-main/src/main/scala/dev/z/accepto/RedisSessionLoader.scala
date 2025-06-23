package dev.z
package accepto

import cats.*
import cats.effect.*
import cats.syntax.all.*
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.effect.MkRedis
import org.typelevel.log4cats

object RedisSessionLoader:
  def load[F[_]: MonadThrow: MkRedis: log4cats.Logger](
    appEnvironment: AppEnvironment
  ): F[Resource[F, RedisCommands[F, String, String]]] =
    implicit val logger: Logger[F] = LoggerImpl.make("RedisSessionLoader")
    implicit val checkConnection: CheckRedisConnection[F] = CheckRedisConnection.make[F]

    loadConfig(appEnvironment).map(RedisSession.make[F])

  def loadConfig[F[_]: Applicative](appEnvironment: AppEnvironment): F[Redis.Config] =
    Applicative[F].pure:
      appEnvironment match
        case AppEnvironment.Prod =>
          Redis.Config(
            Redis.Config.URI(RedisUriString("redis://localhost"))
          )
        case AppEnvironment.Test =>
          Redis.Config(
            Redis.Config.URI(RedisUriString("redis://10.123.154.176"))
          )
