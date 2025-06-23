package dev.z
package accepto
package organizations

import cats.MonadThrow
import fs2.Stream

object BoundaryImpl:
  def make[F[_]: MonadThrow](dependencies: Dependencies[F]): Boundary[F] =
    new Boundary[F]:
      override def createOrganization(
        session: UserSession,
        name: OrganizationName,
      ): F[Organization] =
        dependencies.createOrganization(session, name)

      override def addMember(
        session: UserSessionWithOrganization,
        userName: UserName,
        newRole: MembershipRole,
      ): F[Either[AddMemberError, Unit]] =
        dependencies.addMember(session, userName, newRole)

      override def updateMembership(
        session: UserSessionWithOrganization,
        userName: UserName,
        newRole: MembershipRole,
      ): F[Either[UpdateMembershipError, Unit]] =
        dependencies.updateMembership(session, userName, newRole)

      override def streamMembersForOrganization(
        session: UserSessionWithOrganization
      ): Stream[F, Membership] =
        dependencies.streamMembersForOrganization(session)

      override def streamMembershipsForUser(session: UserSession): Stream[F, Membership] =
        dependencies.streamMembershipsForUser(session)

      override def getMembership(
        session: UserSession,
        orgId: OrganizationId,
      ): F[Option[Membership]] =
        dependencies.getMembership(session, orgId)

  trait Dependencies[F[_]] extends Persistence[F]

  def make[F[_]: MonadThrow](
    persistence: Persistence[F]
  ): Boundary[F] =
    make:
      new Dependencies[F]:
        override def createOrganization(
          session: UserSession,
          name: OrganizationName,
        ): F[Organization] =
          persistence.createOrganization(session, name)

        override def addMember(
          session: UserSessionWithOrganization,
          userName: UserName,
          newRole: MembershipRole,
        ): F[Either[AddMemberError, Unit]] =
          persistence.addMember(session, userName, newRole)

        override def updateMembership(
          session: UserSessionWithOrganization,
          userName: UserName,
          newRole: MembershipRole,
        ): F[Either[UpdateMembershipError, Unit]] =
          persistence.updateMembership(session, userName, newRole)

        override def streamMembersForOrganization(
          session: UserSessionWithOrganization
        ): Stream[F, Membership] =
          persistence.streamMembersForOrganization(session)

        override def streamMembershipsForUser(
          session: UserSession
        ): Stream[F, Membership] =
          persistence.streamMembershipsForUser(session)

        override def getMembership(
          session: UserSession,
          orgId: OrganizationId,
        ): F[Option[Membership]] =
          persistence.getMembership(session, orgId)
