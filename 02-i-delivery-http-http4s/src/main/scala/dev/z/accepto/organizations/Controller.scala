package dev.z
package accepto
package organizations

import java.util.UUID

import cats.*
import cats.syntax.all.*
import fs2.Stream
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.*

import dev.z.accepto.organizations.CirceCodecs.given
import dev.z.accepto.users.auth.CirceCodecs.given
import dev.z.neotypeIntegration.NeotypeRequestDecoder

// Extractor object to pattern-match a valid OrganizationId from a String.
object OrganizationIdVar:
  def unapply(str: String): Option[OrganizationId] =
    try Some(OrganizationId(UUID.fromString(str)))
    catch case _: Exception => None

object ControllerImpl:
  def make[F[_]: JsonDecoder: MonadThrow](dependencies: Dependencies[F]): Controller[F] =
    new Controller.Common[F] with Http4sDsl[F]:

      given EntityEncoder[F, Stream[F, Membership]] =
        streamJsonArrayEncoderOf[F, Membership]

      private def withOrgSession(
        user: CommonUserSession
      )(
        f: UserSessionWithOrganization => F[Response[F]]
      ): F[Response[F]] =
        user
          .requireOrganization
          .fold(
            err => BadRequest(s"Organization not selected: $err"),
            orgSession => f(orgSession),
          )

      override lazy val routes: HttpRoutes[F] =
        Router(
          "/org" -> dependencies.authMiddleware(
            AuthedRoutes.of[CommonUserSession, F]:
              // Create an organization
              case ar @ POST -> Root / "create" as userSession =>
                ar.req.decodeR[CreateOrganizationRequest] { req =>
                  dependencies
                    .createOrganization(userSession, req.name)
                    .flatMap(org => Created(org))
                }

              // Add a member to the organization
              case ar @ POST -> Root / "addMember" as userSession =>
                withOrgSession(userSession) { orgSession =>
                  ar.req.decodeR[AddMemberRequest] { req =>
                    dependencies
                      .addMember(orgSession, req.userName, req.newRole)
                      .flatMap {
                        case Left(error) => BadRequest(error.toString)
                        case Right(_) => Ok()
                      }
                  }
                }

              // Update membership for the organization
              case ar @ POST -> Root / "updateMembership" as userSession =>
                withOrgSession(userSession) { orgSession =>
                  ar.req.decodeR[UpdateMembershipRequest] { req =>
                    dependencies
                      .updateMembership(orgSession, req.userName, req.newRole)
                      .flatMap {
                        case Left(error) => BadRequest(error.toString)
                        case Right(_) => Ok()
                      }
                  }
                }

              // Get a membership for a given organization ID using the extractor
              case ar @ GET -> Root / "membership" / OrganizationIdVar(orgId) as userSession =>
                dependencies.getMembership(userSession, orgId).flatMap {
                  case Some(membership) => Ok(membership)
                  case None => NotFound()
                }

              // Stream all members for an organization
              case ar @ GET -> Root / "members" as userSession =>
                withOrgSession(userSession) { orgSession =>
                  Ok(dependencies.streamMembersForOrganization(orgSession))
                }

              // Stream all memberships for a user
              case ar @ GET -> Root / "memberships" as userSession =>
                Ok(dependencies.streamMembershipsForUser(userSession))
          )
        )

      // Request models with derived codecs
      final case class CreateOrganizationRequest(name: OrganizationName) derives Codec
      final case class AddMemberRequest(
        userName: UserName,
        newRole: MembershipRole,
      ) derives Codec
      final case class UpdateMembershipRequest(
        userName: UserName,
        newRole: MembershipRole,
      ) derives Codec

  // Dependencies aggregates the auth middleware and the Boundary
  trait Dependencies[F[_]] extends HasAuthMiddleware[F, CommonUserSession] with Boundary[F]

  def make[F[_]: JsonDecoder: MonadThrow](
    _authMiddleware: AuthMiddleware[F, CommonUserSession],
    boundary: Boundary[F],
  ): Controller[F] =
    make:
      new Dependencies[F]:
        override def authMiddleware: AuthMiddleware[F, CommonUserSession] =
          _authMiddleware

        override def createOrganization(
          session: UserSession,
          name: OrganizationName,
        ): F[Organization] =
          boundary.createOrganization(session, name)

        override def addMember(
          session: UserSessionWithOrganization,
          userName: UserName,
          newRole: MembershipRole,
        ): F[Either[AddMemberError, Unit]] =
          boundary.addMember(session, userName, newRole)

        override def updateMembership(
          session: UserSessionWithOrganization,
          userName: UserName,
          newRole: MembershipRole,
        ): F[Either[UpdateMembershipError, Unit]] =
          boundary.updateMembership(session, userName, newRole)

        override def streamMembersForOrganization(
          session: UserSessionWithOrganization
        ): Stream[F, Membership] =
          boundary.streamMembersForOrganization(session)

        override def streamMembershipsForUser(
          session: UserSession
        ): Stream[F, Membership] =
          boundary.streamMembershipsForUser(session)

        override def getMembership(
          session: UserSession,
          orgId: OrganizationId,
        ): F[Option[Membership]] =
          boundary.getMembership(session, orgId)
