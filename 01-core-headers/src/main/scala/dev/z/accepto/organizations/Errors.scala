package dev.z
package accepto
package organizations

import scala.util.control.NoStackTrace

enum AddMemberError extends NoStackTrace:
  case MemberNotAuthorized(userId: UserId, orgId: OrganizationId)
  case UserDoesNotExist(userName: UserName)
  case MemberAlreadyExists(userName: UserName, orgId: OrganizationId)

enum UpdateMembershipError extends NoStackTrace:
  case MemberNotAuthorized(userId: UserId, orgId: OrganizationId)
  case MemberDoesNotExist(userName: UserName, orgId: OrganizationId)
