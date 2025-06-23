package dev.z
package accepto
package organizations

import cats.effect.*
import cats.syntax.all.*
import skunk.*
import skunk.implicits.*

object PersistenceImpl:
  def make[F[_]: GenUUID: MonadCancelThrow](
    postgres: Resource[F, Session[F]]
  ): Persistence[F] =
    new Persistence[F]:
      override def createOrganization(
        session: UserSession,
        name: OrganizationName,
      ): F[Organization] =
        postgres.use { s =>
          GenUUID[F].make.map(OrganizationId(_)).flatMap { newOrgId =>
            val org = Organization(newOrgId, name)
            s.prepare(SQL.createOrganizationWithMembership).flatMap { cmd =>
              cmd.execute((org, session.userId)).as(org)
            }
          }
        }

      override def addMember(
        session: UserSessionWithOrganization,
        userName: UserName,
        newRole: MembershipRole,
      ): F[Either[AddMemberError, Unit]] =
        postgres.use { s =>
          s.prepare(SQL.insertMember).flatMap { cmd =>
            cmd
              .execute((session.orgId, session.userId, userName, newRole))
              .flatMap:
                case completion if completion == skunk.data.Completion.Insert(0) =>
                  AddMemberError
                    .MemberNotAuthorized(session.userId, session.orgId)
                    .asLeft[Unit]
                    .pure[F]
                case _ =>
                  ().asRight[AddMemberError].pure[F]
              .recoverWith:
                case SqlState.UniqueViolation(_) =>
                  AddMemberError
                    .MemberAlreadyExists(userName, session.orgId)
                    .asLeft[Unit]
                    .pure[F]
                case SqlState.NotNullViolation(_) | SqlState.ForeignKeyViolation(_) =>
                  AddMemberError
                    .UserDoesNotExist(userName)
                    .asLeft[Unit]
                    .pure[F]
          }
        }
      override def updateMembership(
        session: UserSessionWithOrganization,
        userName: UserName,
        newRole: MembershipRole,
      ): F[Either[UpdateMembershipError, Unit]] =
        postgres
          .use { s =>
            s.prepare(SQL.updateMembershipRole).flatMap { q =>
              q.unique((session.orgId, session.userId, userName, newRole))
                .flatMap: (updated, isAdmin) =>
                  if updated then ().asRight[UpdateMembershipError].pure[F]
                  else if isAdmin then
                    UpdateMembershipError
                      .MemberDoesNotExist(userName, session.orgId)
                      .asLeft[Unit]
                      .pure[F]
                  else
                    UpdateMembershipError
                      .MemberNotAuthorized(session.userId, session.orgId)
                      .asLeft[Unit]
                      .pure[F]
            }
          }
          .handleErrorWith { e =>
            println(s"Exception in updateMembership: ${e.getMessage}")
            e.raiseError[F, Either[UpdateMembershipError, Unit]]
          }

      override def streamMembersForOrganization(
        session: UserSessionWithOrganization
      ): Stream[F, Membership] =
        Stream.resource(postgres).flatMap { s =>
          Stream.eval(s.prepare(SQL.selectMembers)).flatMap { q =>
            q.stream((session.orgId, session.userId), 1024)
          }
        }

      override def streamMembershipsForUser(
        session: UserSession
      ): Stream[F, Membership] =
        Stream.resource(postgres).flatMap { s =>
          Stream.eval(s.prepare(SQL.selectMemberships)).flatMap { q =>
            q.stream(session.userId, 1024)
          }
        }

      override def getMembership(
        session: UserSession,
        orgId: OrganizationId,
      ): F[Option[Membership]] =
        postgres.use { s =>
          s.prepare(SQL.selectMembership).flatMap { q =>
            q.option((orgId, session.userId))
          }
        }
