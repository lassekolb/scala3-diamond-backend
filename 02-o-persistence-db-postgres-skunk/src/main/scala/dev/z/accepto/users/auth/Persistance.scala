package dev.z
package accepto
package users
package auth

import cats.effect.*
import cats.syntax.all.*
import skunk.*
import skunk.implicits.*

object PersistenceImpl:
  def make[F[_]: GenUUID: MonadCancelThrow](
    postgres: Resource[F, Session[F]]
  ): Persistence[F] =
    new Persistence[F]:
      override def findUser(username: UserName): F[Option[UserWithPassword]] =
        postgres.use { session =>
          session.prepare(SQL.selectUser).flatMap { q =>
            q.option(username)
          }
        }

      override def createUser(
        username: UserName,
        password: EncryptedPassword,
      ): F[Either[UserNameInUse, UserId]] =
        postgres.use { session =>
          session.prepare(SQL.insertUser).flatMap { cmd =>
            GenUUID[F].make.map(UserId(_)).flatMap { id =>
              cmd
                .execute(UserWithPassword(id, username, password))
                .as(id.asRight[UserNameInUse])
                .recoverWith:
                  case SqlState.UniqueViolation(_) =>
                    UserNameInUse(username).asLeft.pure
            }
          }
        }

      override def checkMembership(userSession: UserSession, orgId: OrganizationId): F[Boolean] =
        organizations
          .PersistenceImpl
          .make(postgres)
          .getMembership(userSession, orgId)
          .map(_.isDefined)

      override def getMembership(
        userSession: UserSession,
        orgId: OrganizationId,
      ): F[Option[Membership]] =
        organizations
          .PersistenceImpl
          .make(postgres)
          .getMembership(userSession, orgId)
