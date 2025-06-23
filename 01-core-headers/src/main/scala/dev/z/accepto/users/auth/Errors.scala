package dev.z
package accepto
package users
package auth

import scala.util.control.NoStackTrace

sealed trait AuthError extends NoStackTrace:
  def userName: UserName

final case class InvalidPassword(userName: UserName) extends AuthError
final case class UserNameInUse(userName: UserName) extends AuthError
final case class UserNotFound(userName: UserName) extends AuthError
final case class UserNotAMember(userName: UserName) extends AuthError

enum SessionError[Token] extends NoStackTrace:
  case TokenNotFound(userId: UserId)
  case SessionUpdateFailed(userId: UserId, message: String)
