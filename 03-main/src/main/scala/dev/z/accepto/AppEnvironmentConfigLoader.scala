package dev.z
package accepto

import cats.effect.Async
import ciris.*

object AppEnvironmentConfigLoader:
  def load[F[_]: Async]: F[AppEnvironment] =
    env("ACCEPTO_APP_ENV").as[AppEnvironment].load[F]
