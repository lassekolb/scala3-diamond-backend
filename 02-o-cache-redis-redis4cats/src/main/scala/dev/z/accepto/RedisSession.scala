package dev.z
package accepto

import cats.*
import cats.effect.*
import dev.profunktor.redis4cats.effect.*
import dev.profunktor.redis4cats.{ Redis as Redis4Cats, * }

object RedisSession:
  def make[F[_]: MonadThrow: MkRedis: CheckRedisConnection](
    c: Redis.Config
  ): Resource[F, RedisCommands[F, String, String]] =
    Redis4Cats[F]
      .utf8(c.uri)
      .evalTap(CheckRedisConnection[F].checkRedisConnection)
