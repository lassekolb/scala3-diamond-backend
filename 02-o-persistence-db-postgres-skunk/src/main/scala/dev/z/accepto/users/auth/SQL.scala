package dev.z
package accepto
package users
package auth

import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

object SQL:
  lazy val userId: Codec[UserId] =
    uuid.imap[UserId](UserId(_))(_.unwrap)

  lazy val userName: Codec[UserName] =
    varchar.eimap[UserName] { str =>
      NonEmptyString.make(str).map(UserName(_))
    }(_.unwrap)

  lazy val user: Codec[User] =
    (userId *: userName).imap {
      case id *: name *: EmptyTuple =>
        User(id, name)
    }(u => u.id *: u.name *: EmptyTuple)

  lazy val encPassword: Codec[EncryptedPassword] =
    varchar.imap[EncryptedPassword](EncryptedPassword(_))(_.unwrap)

  lazy val userWithPassword: Codec[UserWithPassword] =
    (userId *: userName *: encPassword).imap {
      case id *: name *: p *: EmptyTuple =>
        UserWithPassword(User(id, name), p)
    }(up => up.user.id *: up.user.name *: up.password *: EmptyTuple)

  // lazy val userWithPassword: Codec[UserWithPassword] =
  //   (user *: encPassword).imap {
  //     case u *: p *: EmptyTuple => UserWithPassword(u, p)
  //   }(up => up.user *: up.password *: EmptyTuple)

  // lazy val userWithPassword: Codec[UserWithPassword] =
  //   (userId *: userName *: encPassword).imap {
  //     case id *: name *: p *: EmptyTuple => UserWithPassword(id, name, p)
  //   }(up => up.user.id *: up.user.name *: up.password *: EmptyTuple)
  lazy val selectUser: Query[UserName, UserWithPassword] =
    sql"""
        SELECT id, name, password FROM users
        WHERE name = $userName
       """.query(userWithPassword)

  lazy val insertUser: Command[UserWithPassword] =
    sql"""
        INSERT INTO users (id, name, password)
        VALUES ($userWithPassword)
        """.command
