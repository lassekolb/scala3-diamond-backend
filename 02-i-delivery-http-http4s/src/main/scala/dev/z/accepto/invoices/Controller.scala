package dev.z
package accepto
package invoices

import cats.*
import cats.effect.Concurrent
import cats.syntax.all.*
import fs2.Stream
import io.circe.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.*

import dev.z.accepto.invoices.CirceCodecs.given
import dev.z.neotypeIntegration.NeotypeRequestDecoder

object ControllerImpl:
  def make[F[_]: Concurrent: JsonDecoder: MonadThrow](
    dependencies: Dependencies[F]
  ): Controller[F] =
    new Controller.Common[F] with Http4sDsl[F]:

      given EntityEncoder[F, Stream[F, Invoice]] =
        streamJsonArrayEncoderOf[F, Invoice]

      given QueryParamDecoder[InvoiceDate] =
        QueryParamDecoder[String].emap { str =>
          InvoiceDate.fromString(str).left.map { errorMsg =>
            ParseFailure(s"Failed to parse date: $str", errorMsg)
          }
        }
      given Semigroup[HttpRoutes[F]] with
        def combine(x: HttpRoutes[F], y: HttpRoutes[F]): HttpRoutes[F] =
          x.combineK(y)

      private def withOrgSession(
        user: CommonUserSession
      )(
        f: UserSessionWithOrganization => F[Response[F]]
      ): F[Response[F]] =
        user
          .requireOrganization
          .fold(
            err => BadRequest(s"Organization not selected: $err"),
            f,
          )

      override lazy val routes: HttpRoutes[F] =
        Router(
          "/invoices" -> dependencies.authMiddleware(
            AuthedRoutes.of[CommonUserSession, F]:
              // Create single invoice
              case ar @ POST -> Root as userSession =>
                withOrgSession(userSession) { orgSession =>
                  ar.req.decodeR[CreateInvoice] { createInvoice =>
                    dependencies
                      .createInvoice(orgSession, createInvoice)
                      .flatMap(Created(_))
                  }
                }

              // Create multiple invoices
              case ar @ POST -> Root / "batch" as user =>
                withOrgSession(user) { orgSession =>
                  ar.req.decodeR[List[CreateInvoice]] { createInvoices =>
                    dependencies
                      .createInvoices(orgSession, createInvoices)
                      .flatMap(Created(_))
                  }
                }

              // Find invoice by ID
              case GET -> Root / InvoiceIdVar(invoiceId) as user =>
                withOrgSession(user) { orgSession =>
                  dependencies
                    .findInvoice(orgSession, invoiceId)
                    .flatMap(_.fold(NotFound())(Ok(_)))
                }

              // Stream all invoices by organization
              case GET -> Root as user =>
                withOrgSession(user) { orgSession =>
                  val stream = dependencies.streamInvoicesByOrganization(orgSession)
                  Ok(stream)
                }

              // Stream invoices by date range
              case GET -> Root / "date" :?
                   StartDateQueryParam(startDate) +& EndDateQueryParam(endDate) as user =>
                withOrgSession(user) { orgSession =>
                  val stream = dependencies.streamInvoicesByDate(orgSession, startDate, endDate)
                  Ok(stream)
                }

              // Stream invoices by IDs
              case ar @ POST -> Root / "by-ids" as user =>
                withOrgSession(user) { orgSession =>
                  ar.req.decodeR[List[InvoiceId]] { ids =>
                    val stream = dependencies.streamInvoicesByIds(orgSession, ids)
                    Ok(stream)
                  }
                }
          )
        )

      // Query param matchers
      object StartDateQueryParam extends QueryParamDecoderMatcher[InvoiceDate]("start")
      object EndDateQueryParam extends QueryParamDecoderMatcher[InvoiceDate]("end")

      // Path variable extractor
      object InvoiceIdVar:
        def unapply(str: String): Option[InvoiceId] =
          InvoiceId.fromString(str).toOption

  trait Dependencies[F[_]] extends HasAuthMiddleware[F, CommonUserSession] with Boundary[F]

  def make[F[_]: Concurrent: JsonDecoder: MonadThrow](
    _authMiddleware: AuthMiddleware[F, CommonUserSession],
    boundary: Boundary[F],
  ): Controller[F] =
    make:
      new Dependencies[F]:
        override def authMiddleware: AuthMiddleware[F, CommonUserSession] =
          _authMiddleware

        override def createInvoice(
          session: UserSessionWithOrganization,
          createInvoice: CreateInvoice,
        ): F[InvoiceId] =
          boundary.createInvoice(session, createInvoice)

        override def createInvoicesStream(
          session: UserSessionWithOrganization,
          createInvoice: Stream[F, CreateInvoice],
          batchSize: Int,
        ): Stream[F, InvoiceId] =
          boundary.createInvoicesStream(session, createInvoice, batchSize)

        override def createInvoices(
          session: UserSessionWithOrganization,
          createInvoices: List[CreateInvoice],
        ): F[List[InvoiceId]] =
          boundary.createInvoices(session, createInvoices)

        override def findInvoice(
          session: UserSessionWithOrganization,
          invoiceId: InvoiceId,
        ): F[Option[Invoice]] =
          boundary.findInvoice(session, invoiceId)

        override def streamInvoicesByOrganization(
          session: UserSessionWithOrganization
        ): Stream[F, Invoice] =
          boundary.streamInvoicesByOrganization(session)

        override def streamInvoicesByDate(
          session: UserSessionWithOrganization,
          startDate: InvoiceDate,
          endDate: InvoiceDate,
        ): Stream[F, Invoice] =
          boundary.streamInvoicesByDate(session, startDate, endDate)

        override def streamInvoicesByIds(
          session: UserSessionWithOrganization,
          ids: List[InvoiceId],
        ): Stream[F, Invoice] =
          boundary.streamInvoicesByIds(session, ids)
