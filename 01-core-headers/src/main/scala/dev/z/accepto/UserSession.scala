package dev.z
package accepto

import cats.syntax.show.*

sealed trait UserSession derives Eq:
  def user: User
  def userId: UserId =
    user.id

sealed trait UserSessionWithOrganization extends UserSession:
  def organization: Organization
  def orgId: OrganizationId =
    organization.id

// Generic session type parameterized by the user type
sealed trait Session[U <: User] extends UserSession:
  def user: U

object Session:
  // Generic constructor
  def apply[U <: User](user: U, organization: Option[Organization] = None): Session[U] =
    organization.fold(WithoutOrg(user))(WithOrg(user, _))

  // Generic Show instance
  given [U <: User: Show]: Show[Session[U]] =
    Show.show:
      case WithoutOrg(u) => s"Session.WithoutOrg(user=${u.show})"
      case WithOrg(u, org) => s"Session.WithOrg(user=${u.show}, org=${org.show})"

  final case class WithoutOrg[U <: User](user: U) extends Session[U] derives Eq
  final case class WithOrg[U <: User](user: U, organization: Organization)
      extends Session[U],
              UserSessionWithOrganization derives Eq

// CommonUserSession
type CommonUserSession = Session[CommonUser]
object CommonUserSession:
  def fromUser(userId: UserId, userName: UserName): CommonUserSession =
    Session(CommonUser(User(userId, userName)))

// AdminUserSession
type AdminUserSession = Session[AdminUser]
object AdminUserSession:
  def apply(user: AdminUser, organization: Option[Organization] = None): AdminUserSession =
    Session(user, organization)

extension (session: UserSession)
  def requireOrganization: Either[OrganizationError, UserSessionWithOrganization] =
    session match
      case s: UserSessionWithOrganization => Right(s)
      case _ => Left(OrganizationError.NoOrganizationSelected(session.userId))
