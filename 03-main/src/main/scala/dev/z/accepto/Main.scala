package dev.z
package accepto

import cats.effect.*
import natchez.Trace.Implicits.noop

object Main extends IOApp.Simple:
  override lazy val run: IO[Unit] =
    Program.make[IO]
