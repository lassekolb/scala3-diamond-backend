package dev.z
package accepto
package organizations

import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

import dev.z.accepto.users.auth.SQL.*

object SQL:
  lazy val organizationId: Codec[OrganizationId] =
    uuid.imap(OrganizationId(_))(_.unwrap)

  lazy val organizationName: Codec[OrganizationName] =
    varchar.eimap[OrganizationName] { str =>
      NonEmptyString.make(str).map(OrganizationName(_))
    }(_.unwrap)

  lazy val organization: Codec[Organization] =
    (organizationId *: organizationName)
      .imap(Organization.apply)(org => (org.id, org.name))

  lazy val membershipRole: Codec[MembershipRole] =
    varchar.imap[MembershipRole](
      {
        case "Admin" => MembershipRole.Admin
        case "Member" => MembershipRole.Member
      }
    )(
      {
        case MembershipRole.Admin => "Admin"
        case MembershipRole.Member => "Member"
      }
    )

  lazy val membership: Codec[Membership] =
    (userId *: userName *: organizationId *: organizationName *: membershipRole).imap {
      case uid *: uname *: oid *: oname *: role *: EmptyTuple =>
        Membership(User(uid, uname), Organization(oid, oname), role)
    } { m =>
      m.user.id *: m.user.name *: m.organization.id *: m.organization.name *: m.role *: EmptyTuple
    }

  lazy val createOrganizationWithMembership: Command[(Organization, UserId)] =
    sql"""
      WITH new_org AS (
        INSERT INTO organizations (id, name)
        VALUES ($organizationId, $organizationName)
        RETURNING id
      )
      INSERT INTO memberships (user_id, organization_id, role)
      SELECT $userId, id, 'Admin'
      FROM new_org
    """
      .command
      .contramap:
        case (org, userId) =>
          org.id *: org.name *: userId *: EmptyTuple

  lazy val selectMemberships: Query[UserId, Membership] =
    sql"""
      SELECT u.id, u.name, o.id, o.name, m.role
      FROM memberships AS m
      INNER JOIN users AS u ON m.user_id = u.id
      INNER JOIN organizations AS o ON m.organization_id = o.id
      WHERE u.id = $userId
    """.query(membership)

  lazy val selectMembership: Query[(OrganizationId, UserId), Membership] =
    sql"""
      SELECT u.id, u.name, o.id, o.name, m.role
      FROM memberships AS m
      INNER JOIN users AS u ON m.user_id = u.id
      INNER JOIN organizations AS o ON m.organization_id = o.id
      WHERE o.id = $organizationId AND u.id = $userId
    """.query(membership)

  lazy val updateMembershipRole: Query[(OrganizationId, UserId, UserName, MembershipRole), (Boolean, Boolean)] =
    sql"""
      WITH admin_check AS (
        SELECT EXISTS(
          SELECT 1
          FROM memberships
          WHERE organization_id = $organizationId
            AND user_id = $userId
            AND role = 'Admin'
        ) AS is_admin
      ),
      target AS (
        SELECT id FROM users WHERE name = $userName LIMIT 1
      ),
      upd AS (
        UPDATE memberships
        SET role = $membershipRole
        WHERE organization_id = $organizationId
          AND user_id = (SELECT id FROM target)
          AND (SELECT is_admin FROM admin_check)
        RETURNING 1
      )
      SELECT EXISTS (SELECT 1 FROM upd) AS updated, (SELECT is_admin FROM admin_check) AS admin_success
    """
      .query(bool ~ bool)
      .contramap:
        case (orgId, sessionUser, targetUserName, newRole) =>
          orgId *: sessionUser *: targetUserName *: newRole *: orgId *: EmptyTuple

  lazy val insertMember: Command[(OrganizationId, UserId, UserName, MembershipRole)] =
    sql"""
      WITH admin_check AS (
        SELECT 1
        FROM memberships
        WHERE user_id = $userId
          AND organization_id = $organizationId
          AND role = 'Admin'
      ),
      target AS (
        SELECT id FROM users WHERE name = $userName LIMIT 1
      )
      INSERT INTO memberships (user_id, organization_id, role)
      SELECT target.id, $organizationId, $membershipRole
      FROM target
      WHERE EXISTS (SELECT 1 FROM admin_check)
    """
      .command
      .contramap:
        case (orgId, sessionUser, targetUser, role) =>
          sessionUser *: orgId *: targetUser *: orgId *: role *: EmptyTuple

  lazy val selectMembers: Query[(OrganizationId, UserId), Membership] =
    sql"""
      SELECT u.id, u.name, o.id, o.name, m.role
      FROM memberships AS m
      INNER JOIN users AS u ON m.user_id = u.id
      INNER JOIN organizations AS o ON m.organization_id = o.id
      WHERE o.id = $organizationId
        AND EXISTS (
          SELECT 1 FROM memberships
          WHERE user_id = $userId
            AND organization_id = $organizationId
        )
    """
      .query(membership)
      .contramap:
        case (orgId, userId) =>
          orgId *: userId *: orgId *: EmptyTuple
