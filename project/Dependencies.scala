import sbt._

object Dependencies {

  // case object co {
  //   case object fs2 {
  //     val `fs2-core` =
  //       "co.fs2" %% "fs2-core" % "3.11.0"
  //   }
  // }
  //
  // case object com {
  //   case object github {
  //     val `jwt-scala` =
  //       "com.github.jwt-scala" %% "jwt-circe" % "10.0.4"
  //   }
  // }
  //
  // case object dev {
  //   case object zio {
  //     val zio =
  //       "dev.zio" %% "zio" % "2.1.15"
  //
  //     val `zio-interop-cats` =
  //       "dev.zio" %% "zio-interop-cats" % "23.1.0.3"
  //   }
  // }
  //
  // case object io {
  //   case object circe {
  //     val `circe-generic` =
  //       dependency("generic")
  //
  //     private def dependency(artifact: String): ModuleID =
  //       "io.circe" %% s"circe-$artifact" % "0.14.10"
  //   }
  // }
  //
  // case object org {
  //   case object http4s {
  //     val `http4s-ember-server` =
  //       dependency("ember-server")
  //
  //     val `http4s-circe` =
  //       dependency("circe")
  //
  //     val `http4s-dsl` =
  //       dependency("dsl")
  //
  //     val `http4s-client` =
  //       dependency("client")
  //
  //     private def dependency(artifact: String): ModuleID =
  //       "org.http4s" %% s"http4s-$artifact" % "0.23.30"
  //   }
  //
  //   case object scalacheck {
  //     val scalacheck =
  //       "org.scalacheck" %% "scalacheck" % "1.18.1"
  //   }
  //
  //   case object scalatest {
  //     val scalatest =
  //       "org.scalatest" %% "scalatest" % "3.2.19"
  //   }
  //
  //   case object scalatestplus {
  //     val `scalacheck-1-18` =
  //       "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0"
  //   }
  //
  //   case object slf4j {
  //     val `slf4j-simple` =
  //       "org.slf4j" % "slf4j-simple" % "2.0.16"
  //   }
  //
  //   case object tpolecat {
  //     val `skunk-core` =
  //       "org.tpolecat" %% "skunk-core" % "0.6.4"
  //   }
  //
  //   case object typelevel {
  //     val `cats-core` =
  //       "org.typelevel" %% "cats-core" % "2.13.0"
  //
  //     val `cats-effect` =
  //       "org.typelevel" %% "cats-effect" % "3.5.7"
  //
  //     val `discipline-scalatest` =
  //       "org.typelevel" %% "discipline-scalatest" % "2.3.0"
  //
  //     val shapeless3 =
  //       "org.typelevel" %% "shapeless3-deriving" % "3.4.3"
  //
  //     val `munit-cats-effect-3` =
  //       "org.typelevel" %% "munit-cats-effect-3" % "1.0.7"
  //
  //     val squants =
  //       "org.typelevel" %% "squants" % "1.8.3"
  //   }
  // }
  //
  object V {
    val cats =
      "2.13.0"
    val catsEffect =
      "3.5.7"
    val catsRetry =
      "4.0.0"
    val circe =
      "0.14.10"
    val ciris =
      "3.7.0"
    val kittens =
      "3.4.0"
    // val derevo =
    //   "0.13.6"
    val javaxCrypto =
      "1.0.1"
    val fs2 =
      "3.11.0"
    val http4s =
      "0.23.30"
    val http4sJwtAuth =
      "2.0.2"
    val log4cats =
      "2.2.0"
    // val monocle =
    //   "3.3.0"
    // val newtype =
    //   "0.4.4"
    val neotype =
      "0.3.15"
    // val refined =
    //   "0.11.3"
    val redis4cats =
      "1.7.2"
    val skunk =
      "0.6.4"
    val squants =
      "1.8.3"
    val logback =
      "1.2.11"
    // val organizeImports =
    //   "0.6.0"
    // val semanticDB =
    //   "4.5.4"

    val weaver =
      "0.7.11"
  }

  object Libraries {
    def circe(artifact: String): ModuleID =
      "io.circe" %% s"circe-$artifact" % V.circe
    def ciris(artifact: String): ModuleID =
      "is.cir" %% artifact % V.ciris
    def http4s(artifact: String): ModuleID =
      "org.http4s" %% s"http4s-$artifact" % V.http4s

    val cats =
      "org.typelevel" %% "cats-core" % V.cats
    val catsEffect =
      "org.typelevel" %% "cats-effect" % V.catsEffect
    val catsRetry =
      "com.github.cb372" %% "cats-retry" % V.catsRetry
    val squants =
      "org.typelevel" %% "squants" % V.squants
    val fs2 =
      "co.fs2" %% "fs2-core" % V.fs2

    val kittens =
      "org.typelevel" %% "kittens" % V.kittens

    val circeCore =
      circe("core")
    val circeGeneric =
      circe("generic")
    val circeParser =
      circe("parser")
    // val circeRefined =
    //   circe("refined")

    val cirisCore =
      ciris("ciris")
    val cirisEnum =
      ciris("ciris-enumeratum")
    // val cirisRefined =
    //   ciris("ciris-refined")

    // val derevoCore =
    //   derevo("core")
    // val derevoCats =
    //   derevo("cats")
    // val derevoCirce =
    //   derevo("circe-magnolia")

    val http4sDsl =
      http4s("dsl")
    val http4sServer =
      http4s("ember-server")
    val http4sClient =
      http4s("ember-client")
    val http4sCirce =
      http4s("circe")

    val http4sJwtAuth =
      "dev.profunktor" %% "http4s-jwt-auth" % V.http4sJwtAuth

    // val monocleCore =
    //   "dev.optics" %% "monocle-core" % V.monocle

    // val refinedCore =
    //   "eu.timepit" %% "refined" % V.refined
    // val refinedCats =
    //   "eu.timepit" %% "refined-cats" % V.refined

    val log4cats =
      "org.typelevel" %% "log4cats-slf4j" % V.log4cats
    // val newtype =
    //   "io.estatico" %% "newtype" % V.newtype
    val neotype =
      "io.github.kitlangton" %% "neotype" % V.neotype
    val neotypeCirce =
      "io.github.kitlangton" %% "neotype-circe" % V.neotype
    val neotypeCiris =
      "io.github.kitlangton" %% "neotype-ciris" % V.neotype

    val javaxCrypto =
      "javax.xml.crypto" % "jsr105-api" % V.javaxCrypto

    val redis4catsEffects =
      "dev.profunktor" %% "redis4cats-effects" % V.redis4cats
    val redis4catsLog4cats =
      "dev.profunktor" %% "redis4cats-log4cats" % V.redis4cats

    val skunkCore =
      "org.tpolecat" %% "skunk-core" % V.skunk
    val skunkCirce =
      "org.tpolecat" %% "skunk-circe" % V.skunk

    // Runtime
    val logback =
      "ch.qos.logback" % "logback-classic" % V.logback

    // Test
    val catsLaws =
      "org.typelevel" %% "cats-laws" % V.cats
    val log4catsNoOp =
      "org.typelevel" %% "log4cats-noop" % V.log4cats
    // val monocleLaw =
    //   "dev.optics" %% "monocle-law" % V.monocle
    // val refinedScalacheck =
    //   "eu.timepit" %% "refined-scalacheck" % V.refined
    val weaverCats =
      "com.disneystreaming" %% "weaver-cats" % V.weaver
    val weaverDiscipline =
      "com.disneystreaming" %% "weaver-discipline" % V.weaver
    val weaverScalaCheck =
      "com.disneystreaming" %% "weaver-scalacheck" % V.weaver
    // Scalafix rules
    // val organizeImports =
    // "com.github.liancheng" %% "organize-imports" % V.organizeImports
  }
  // object CompilerPlugin {
  //   val betterMonadicFor =
  //     compilerPlugin(
  //       "com.olegpy" %% "better-monadic-for" % V.betterMonadicFor
  //     )
  //   val kindProjector =
  //     compilerPlugin(
  //       "org.typelevel" % "kind-projector" % V.kindProjector cross CrossVersion.full
  //     )
  //   val semanticDB =
  //     compilerPlugin(
  //       "org.scalameta" % "semanticdb-scalac" % V.semanticDB cross CrossVersion.full
  //     )
  // }
}
