package dev.z
package accepto
package organizations

trait Persistence[F[_]]:
  def createOrganization(
    session: UserSession,
    name: OrganizationName,
  ): F[Organization]

  def addMember(
    session: UserSessionWithOrganization,
    userName: UserName,
    newRole: MembershipRole,
  ): F[Either[AddMemberError, Unit]]

  def updateMembership(
    session: UserSessionWithOrganization,
    userName: UserName,
    newRole: MembershipRole,
  ): F[Either[UpdateMembershipError, Unit]]

  def streamMembersForOrganization(
    session: UserSessionWithOrganization
  ): Stream[F, Membership]

  def streamMembershipsForUser(
    session: UserSession
  ): Stream[F, Membership]

  def getMembership(
    session: UserSession,
    orgId: OrganizationId,
  ): F[Option[Membership]]
