package dev.z
package accepto
package users
package auth

final case class UserWithPassword(
  user: User,
  password: EncryptedPassword,
):
  def id: UserId =
    user.id
  def name: UserName =
    user.name

object UserWithPassword:
  def apply(
    id: UserId,
    name: UserName,
    password: EncryptedPassword,
  ): UserWithPassword =
    UserWithPassword(User(id, name), password)
