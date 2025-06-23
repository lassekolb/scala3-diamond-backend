// =============================================
// OSS Metadata
// =============================================
organization := "dev.z"
name := "accepto"
version := "0.0.1-SNAPSHOT"
licenses += ("MIT", url("https://opensource.org/licenses/MIT"))

import Dependencies.*
// import MyUtil._

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / organization := "dev.z"
ThisBuild / scalaVersion := "3.3.5"
ThisBuild / version := "0.0.1-SNAPSHOT"

ThisBuild / evictionErrorLevel := Level.Warn

Compile / run / fork := true
Test / fork := true

resolvers ++= Resolver.sonatypeOssRepos("snapshots")
// val scalafixCommonSettings = inConfig(IntegrationTest)(scalafixConfigSettings(IntegrationTest))

lazy val root =
  (project in file("."))
    .settings(
      name := "accepto",
      Compile / run / fork := true,
    )
    .aggregate(
      `core-headers`,
      core,
      // c
      `json-circe`,
      `retries-cats-retry`,
      // i
      `delivery-http-http4s`,
      // o
      `auth-jwt-pdi`,
      `cache-redis-redis4cats`,
      `client-http-http4s`,
      `config-env-ciris`,
      `core-adapters`,
      `cryptography-jsr105-api`,
      `persistence-db-postgres-skunk`,
      // main
      main,
      // old stuff
      // `big-ball-of-mud`,
      // `small-ball-of-mud`,
      // tests
    )

// lazy val tests = (project in file("modules/tests"))
//   .configs(IntegrationTest)
//   .settings(
//     name := "accepto-test-suite",
//     scalacOptions ++= List("-Ymacro-annotations", "-Yrangepos", "-Wconf:cat=unused:info"),
//     testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
//     Defaults.itSettings,
//     scalafixCommonSettings,
//     libraryDependencies ++= Seq(
//       CompilerPlugin.kindProjector,
//       CompilerPlugin.betterMonadicFor,
//       CompilerPlugin.semanticDB,
//       Libraries.catsLaws,
//       Libraries.log4catsNoOp,
//       Libraries.monocleLaw,
//       Libraries.refinedScalacheck,
//       Libraries.weaverCats,
//       Libraries.weaverDiscipline,
//       Libraries.weaverScalaCheck
//     )
//   )
// .dependsOn(`big-ball-of-mud`)

lazy val `core-headers` =
  project
    .in(file("01-core-headers"))
    .settings(
      scalacOptions ++= List("-deprecation", "-Wunused:all"),
      resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
      libraryDependencies ++= Seq(
//        CompilerPlugin.kindProjector,
//        CompilerPlugin.betterMonadicFor,
//        CompilerPlugin.semanticDB,
        Libraries.cats,
        Libraries.kittens,
        // Libraries.derevoCats,
        // Libraries.derevoCore,
        // Libraries.monocleCore,
        Libraries.neotype,
        // Libraries.newtype,
        // Libraries.refinedCats,
        // Libraries.refinedCore,
        Libraries.squants,
        Libraries.fs2,
      ),
    )

lazy val core =
  project
    .in(file("02-o-core"))
    .dependsOn(
      Seq(
        `core-headers`
      ).map(_ % Cctt): _*
    )
    .settings(
      scalacOptions ++= List("-deprecation", "-Wunused:all"),
      resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
      libraryDependencies ++= Seq(
//        CompilerPlugin.kindProjector,
//        CompilerPlugin.betterMonadicFor,
//        CompilerPlugin.semanticDB,
        Libraries.cats,
        // Libraries.derevoCats,
        // Libraries.derevoCore,
        // Libraries.monocleCore,
        // Libraries.newtype,
        // Libraries.refinedCats,
        // Libraries.refinedCore,
        Libraries.squants,
      ),
    )

lazy val `retries-cats-retry` =
  project
    .in(file("02-c-retries-cats-retry"))
    .dependsOn(
      Seq(
        `core-headers`
      ).map(_ % Cctt): _*
    )
    .settings(
      scalacOptions ++= List("-deprecation", "-Wunused:all"),
      resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
      libraryDependencies ++= Seq(
        //       CompilerPlugin.kindProjector,
        //       CompilerPlugin.betterMonadicFor,
        //       CompilerPlugin.semanticDB,
        Libraries.catsEffect,
        Libraries.catsRetry,
      ),
    )

lazy val `json-circe` =
  project
    .in(file("02-c-json-circe"))
    .dependsOn(
      Seq(
        `core-headers`
      ).map(_ % Cctt): _*
    )
    .settings(
      scalacOptions ++= List("-deprecation", "-Wunused:all"),
      resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
      libraryDependencies ++= Seq(
        //      CompilerPlugin.kindProjector,
        //      CompilerPlugin.betterMonadicFor,
        //      CompilerPlugin.semanticDB,
        Libraries.circeCore,
        Libraries.circeGeneric,
        Libraries.circeParser,
        // Libraries.neotype,
        Libraries.neotypeCirce,
        // Libraries.circeRefined,
        // Libraries.derevoCirce
      ),
    )

lazy val `delivery-http-http4s` =
  project
    .in(file("02-i-delivery-http-http4s"))
    .dependsOn(
      Seq(
        `core-headers`,
        `json-circe`,
      ).map(_ % Cctt): _*
    )
    .settings(
      scalacOptions ++= List("-deprecation", "-Wunused:all"),
      resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
      libraryDependencies ++= Seq(
//        CompilerPlugin.kindProjector,
//        CompilerPlugin.betterMonadicFor,
//        CompilerPlugin.semanticDB,
        Libraries.catsEffect,
        Libraries.http4sCirce,
        Libraries.http4sDsl,
        Libraries.http4sJwtAuth,
        Libraries.http4sServer,
      ),
    )

lazy val `auth-jwt-pdi` =
  project
    .in(file("02-o-auth-jwt-pdi"))
    .dependsOn(
      Seq(
        `json-circe`
      ).map(_ % Cctt): _*
    )
    .settings(
      scalacOptions ++= List("-deprecation", "-Wunused:all"),
      resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
      libraryDependencies ++= Seq(
//        CompilerPlugin.kindProjector,
//        CompilerPlugin.betterMonadicFor,
//        CompilerPlugin.semanticDB,
        Libraries.catsEffect,
        Libraries.http4sJwtAuth, // this is a wrapper around http4s and jwt-scala
      ),
    )

lazy val `cache-redis-redis4cats` =
  project
    .in(file("02-o-cache-redis-redis4cats"))
    .dependsOn(
      Seq(
        `json-circe`
      ).map(_ % Cctt): _*
    )
    .settings(
      scalacOptions ++= List("-deprecation", "-Wunused:all"),
      resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
      libraryDependencies ++= Seq(
//        CompilerPlugin.kindProjector,
//        CompilerPlugin.betterMonadicFor,
//        CompilerPlugin.semanticDB,
        Libraries.catsEffect,
        Libraries.redis4catsEffects,
        Libraries.redis4catsLog4cats,
      ),
    )

lazy val `client-http-http4s` =
  project
    .in(file("02-o-client-http-http4s"))
    .dependsOn(
      Seq(
        `json-circe`
      ).map(_ % Cctt): _*
    )
    .settings(
      scalacOptions ++= List("-deprecation", "-Wunused:all"),
      resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
      libraryDependencies ++= Seq(
//        CompilerPlugin.kindProjector,
//        CompilerPlugin.betterMonadicFor,
//        CompilerPlugin.semanticDB,
        Libraries.catsEffect,
        Libraries.http4sDsl,
        Libraries.http4sClient,
        Libraries.http4sCirce,
      ),
    )

lazy val `config-env-ciris` =
  project
    .in(file("02-o-config-env-ciris"))
    .dependsOn(
      Seq(
        `core-headers`
      ).map(_ % Cctt): _*
    )
    .settings(
      scalacOptions ++= List("-deprecation", "-Wunused:all"),
      resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
      libraryDependencies ++= Seq(
        Libraries.catsEffect,
        Libraries.cirisCore,
        Libraries.cirisEnum,
        Libraries.neotypeCiris,
      ),
    )

lazy val `core-adapters` =
  project
    .in(file("02-o-core-adapters"))
    .dependsOn(
      Seq(
        `core-headers`
      ).map(_ % Cctt): _*
    )
    .settings(
      scalacOptions ++= List("-deprecation", "-Wunused:all"),
      resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
      libraryDependencies ++= Seq(
//        CompilerPlugin.kindProjector,
//        CompilerPlugin.betterMonadicFor,
//        CompilerPlugin.semanticDB
      ),
    )

lazy val `cryptography-jsr105-api` =
  project
    .in(file("02-o-cryptography-jsr105-api"))
    .dependsOn(
      Seq(
        `core-headers`
      ).map(_ % Cctt): _*
    )
    .settings(
      scalacOptions ++= List("-deprecation", "-Wunused:all"),
      resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
      libraryDependencies ++= Seq(
//        CompilerPlugin.kindProjector,
//        CompilerPlugin.betterMonadicFor,
//        CompilerPlugin.semanticDB,
        Libraries.catsEffect,
        Libraries.javaxCrypto,
      ),
    )

lazy val `persistence-db-postgres-skunk` =
  project
    .in(file("02-o-persistence-db-postgres-skunk"))
    .dependsOn(
      Seq(
        `json-circe`,
        `retries-cats-retry`,
      ).map(_ % Cctt): _*
    )
    .settings(
      scalacOptions ++= List("-deprecation", "-Wunused:all"),
      resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
      libraryDependencies ++= Seq(
//        CompilerPlugin.kindProjector,
//        CompilerPlugin.betterMonadicFor,
//        CompilerPlugin.semanticDB,
        Libraries.catsEffect,
        Libraries.fs2,
        Libraries.skunkCirce,
        Libraries.skunkCore,
      ),
    )

lazy val main =
  project
    .in(file("03-main"))
    .dependsOn(
      Seq(
        // i
        `delivery-http-http4s`,
        // o
        `auth-jwt-pdi`,
        `cache-redis-redis4cats`,
        `client-http-http4s`,
        `config-env-ciris`,
        `core-adapters`,
        core,
        `cryptography-jsr105-api`,
        `persistence-db-postgres-skunk`,
      ).map(_ % Cctt): _*
    )
    .settings(
      scalacOptions ++= List("-deprecation", "-Wunused:all"),
      resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
      libraryDependencies ++= Seq(
//        CompilerPlugin.kindProjector,
//        CompilerPlugin.betterMonadicFor,
//        CompilerPlugin.semanticDB,
        Libraries.neotypeCiris,
        Libraries.log4cats,
        Libraries.logback % Runtime,
      ),
    )

// lazy val `small-ball-of-mud` = (project in file("small-ball-of-mud"))
//   .settings(
//     scalacOptions ++= List("-Ymacro-annotations", "-Yrangepos", "-Wconf:cat=unused:info"),
//     resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
//     libraryDependencies ++= Seq(
//       CompilerPlugin.kindProjector,
//       CompilerPlugin.betterMonadicFor,
//       CompilerPlugin.semanticDB,
//       Libraries.cats,
//       Libraries.catsEffect,
//       Libraries.catsRetry,
//       Libraries.circeCore,
//       Libraries.circeGeneric,
//       Libraries.circeParser,
//       Libraries.circeRefined,
//       Libraries.cirisCore,
//       Libraries.cirisEnum,
//       Libraries.cirisRefined,
//       Libraries.derevoCore,
//       Libraries.derevoCats,
//       Libraries.derevoCirce,
//       Libraries.fs2,
//       Libraries.http4sDsl,
//       Libraries.http4sServer,
//       Libraries.http4sClient,
//       Libraries.http4sCirce,
//       Libraries.http4sJwtAuth,
//       Libraries.javaxCrypto,
//       Libraries.log4cats,
//       Libraries.logback % Runtime,
//       Libraries.monocleCore,
//       Libraries.newtype,
//       Libraries.redis4catsEffects,
//       Libraries.redis4catsLog4cats,
//       Libraries.refinedCore,
//       Libraries.refinedCats,
//       Libraries.skunkCore,
//       Libraries.skunkCirce,
//       Libraries.squants
//     )
//   )
//
// lazy val `big-ball-of-mud` = (project in file("modules/core"))
//   .enablePlugins(DockerPlugin)
//   .enablePlugins(AshScriptPlugin)
//   // .dependsOn(main % Cctt)
//   .settings(
//     name := "accepto-core",
//     Docker / packageName := "accepto",
//     scalacOptions ++= List("-Ymacro-annotations", "-Yrangepos", "-Wconf:cat=unused:info"),
//     resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
//     Defaults.itSettings,
//     scalafixCommonSettings,
//     dockerBaseImage := "openjdk:11-jre-slim-buster",
//     dockerExposedPorts ++= Seq(8080),
//     makeBatScripts := Seq(),
//     dockerUpdateLatest := true,
//     libraryDependencies ++= Seq(
//       CompilerPlugin.kindProjector,
//       CompilerPlugin.betterMonadicFor,
//       CompilerPlugin.semanticDB,
//       Libraries.cats,
//       Libraries.catsEffect,
//       Libraries.catsRetry,
//       Libraries.circeCore,
//       Libraries.circeGeneric,
//       Libraries.circeParser,
//       Libraries.circeRefined,
//       Libraries.cirisCore,
//       Libraries.cirisEnum,
//       Libraries.cirisRefined,
//       Libraries.derevoCore,
//       Libraries.derevoCats,
//       Libraries.derevoCirce,
//       Libraries.fs2,
//       Libraries.http4sDsl,
//       Libraries.http4sServer,
//       Libraries.http4sClient,
//       Libraries.http4sCirce,
//       Libraries.http4sJwtAuth,
//       Libraries.javaxCrypto,
//       Libraries.log4cats,
//       Libraries.logback % Runtime,
//       Libraries.monocleCore,
//       Libraries.newtype,
//       Libraries.redis4catsEffects,
//       Libraries.redis4catsLog4cats,
//       Libraries.refinedCore,
//       Libraries.refinedCats,
//       Libraries.skunkCore,
//       Libraries.skunkCirce,
//       Libraries.squants
//     )
//   )

addCommandAlias("runLinter", ";scalafixAll --rules OrganizeImports")

lazy val Cctt: String = "compile->compile;test->test"
