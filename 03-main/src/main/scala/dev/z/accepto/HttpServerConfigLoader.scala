package dev.z
package accepto

import cats.*
import cats.syntax.all.*
import com.comcast.ip4s.*

object HttpServerConfigLoader:
  def load[F[_]: Applicative]: F[HttpServerConfig] =
    HttpServerConfig(
      host = host"0.0.0.0",
      port = port"8080",
    ).pure
