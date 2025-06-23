package dev.z
package accepto
package users
package auth

import cats.*
import cats.data.NonEmptyList
import cats.syntax.all.*
import dev.profunktor.auth.*
import io.circe.*
import io.circe.generic.auto.*
import neotype.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.*

import dev.z.neotypeIntegration.NeotypeRequestDecoder

object ControllerImpl:
  def make[F[_]: JsonDecoder: MonadThrow](
    dependencies: Dependencies[F]
  ): Controller[F] =
    new Controller.Common[F] with Http4sDsl[F]:
      given Semigroup[HttpRoutes[F]] with
        def combine(x: HttpRoutes[F], y: HttpRoutes[F]): HttpRoutes[F] =
          x.combineK(y)
      override lazy val routes: HttpRoutes[F] =
        Router("/auth" -> NonEmptyList.of(notAuthedRoutes, authedRoutes).reduce)

      private lazy val notAuthedRoutes: HttpRoutes[F] =
        HttpRoutes.of[F]:
          case req @ POST -> Root / "users" =>
            req.decodeR[CreateUser](users)

          case req @ POST -> Root / "login" =>
            req.decodeR[LoginUser](login)

      private lazy val authedRoutes: HttpRoutes[F] =
        dependencies.authMiddleware:
          AuthedRoutes.of[CommonUserSession, F]:
            case ar @ POST -> Root / "logout" as userSession =>
              AuthHeaders
                .getBearerToken(ar.req)
                .traverse_(logoutUser(_, userSession.userId)) *> NoContent()

            case POST -> Root / "logout1" as userSession =>
              logoutUser1(userSession) *> NoContent()

            case ar @ POST -> Root / "org" as userSession =>
              ar.req.decodeR[SetOrganization] { org =>
                setOrg(userSession, org.orgId) *> NoContent()
              }

      private def users(createUser: CreateUser): F[Response[F]] =
        dependencies
          .newUser(createUser.username.toDomain, createUser.password.toDomain)
          .flatMap(Created(_))
          .recoverWith:
            case e: UserNameInUse => Conflict(e.toString)

      private def login(loginUser: LoginUser): F[Response[F]] =
        dependencies
          .login(loginUser.username.toDomain, loginUser.password.toDomain)
          .flatMap(Ok(_))
          .recoverWith:
            case _: UserNotFound | _: InvalidPassword => Forbidden()

      private def logoutUser(token: jwt.JwtToken, userId: UserId): F[Unit] =
        dependencies.logout(token, userId)

      private def logoutUser1(session: UserSession): F[Unit] =
        dependencies.logout1(session)

      private def setOrg(session: UserSession, orgId: OrganizationId): F[Unit] =
        dependencies.setOrg(session, orgId)

      given EntityEncoder[F, jwt.JwtToken] =
        CirceEntityEncoder.circeEntityEncoder(summon[Encoder[jwt.JwtToken]])
      given Decoder[NonEmptyString] =
        Decoder[String].emap { s =>
          NonEmptyString.make(s)
        }
      given Encoder[NonEmptyString] =
        Encoder[String].contramap[NonEmptyString](identity)
      given EntityEncoder[F, NonEmptyString] =
        CirceEntityEncoder.circeEntityEncoder(summon[Encoder[NonEmptyString]])

  trait Dependencies[F[_]]
      extends HasAuthMiddleware[F, CommonUserSession]
         with Boundary[F, jwt.JwtToken]

  def make[F[_]: JsonDecoder: MonadThrow](
    _authMiddleware: AuthMiddleware[F, CommonUserSession],
    boundary: Boundary[F, jwt.JwtToken],
  ): Controller[F] =
    make:
      new Dependencies[F]:
        override def authMiddleware: AuthMiddleware[F, CommonUserSession] =
          _authMiddleware

        override def newUser(userName: UserName, password: Password): F[jwt.JwtToken] =
          boundary.newUser(userName, password)

        override def login(userName: UserName, password: Password): F[jwt.JwtToken] =
          boundary.login(userName, password)

        override def logout(token: jwt.JwtToken, userId: UserId): F[Unit] =
          boundary.logout(token, userId)

        override def logout1(session: UserSession): F[Unit] =
          boundary.logout1(session)

        override def setOrg(session: UserSession, orgId: OrganizationId): F[Unit] =
          boundary.setOrg(session, orgId)

  given Encoder[jwt.JwtToken] =
    Encoder.forProduct1("access_token")(_.value)

  given Decoder[NonEmptyString] =
    Decoder[String].emap { s =>
      NonEmptyString.make(s)
    }
  given Encoder[NonEmptyString] =
    Encoder[String].contramap[NonEmptyString](identity)

  object UserNameParam extends Newtype[NonEmptyString]:
    inline override def validate(input: NonEmptyString): Boolean | String =
      val lower = input.toLowerCase
      if input != lower then "Username must be in lowercase"
      else NonEmptyString.validate(lower)

    given Codec[UserNameParam] =
      Codec.from(
        Decoder[NonEmptyString].emap(UserNameParam.make),
        Encoder[NonEmptyString].contramap(_.unwrap),
      )

    extension (unp: UserNameParam) def toDomain: UserName = UserName(unp.unwrap)

  type UserNameParam =
    UserNameParam.Type

  given Codec[OrganizationId] =
    Codec.from(Decoder[UUID].emap(OrganizationId.make), Encoder[UUID].contramap(_.unwrap))

  // Define PasswordParam using Neotype
  object PasswordParam extends Newtype[NonEmptyString]
  given Codec[PasswordParam] =
    Codec.from(
      Decoder[NonEmptyString].emap(PasswordParam.make),
      Encoder[NonEmptyString].contramap(_.unwrap),
    )

  extension (pp: PasswordParam) def toDomain: Password = Password(pp.unwrap)

  type PasswordParam =
    PasswordParam.Type

  final case class CreateUser(username: UserNameParam, password: PasswordParam) derives Codec
  final case class LoginUser(username: UserNameParam, password: PasswordParam) derives Codec
  final case class SetOrganization(orgId: OrganizationId) derives Codec
