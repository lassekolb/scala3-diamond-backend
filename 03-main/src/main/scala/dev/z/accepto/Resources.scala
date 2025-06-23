package dev.z
package accepto

import cats.effect.*
import dev.profunktor.redis4cats.RedisCommands
import skunk.*

final case class Resources[F[_]](
  redis: RedisCommands[F, String, String],
  postgres: Resource[F, Session[F]],
  // clientResources: HttpClientResources[F],
)
