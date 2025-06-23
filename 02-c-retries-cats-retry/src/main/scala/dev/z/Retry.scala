package dev.z

import cats.effect.Temporal
import cats.syntax.flatMap.*
import cats.syntax.show.*
import retry.*
import retry.RetryDetails.*

trait Retry[F[_]]:
  def retry[A](policy: RetryPolicy[F, Throwable], retriable: Retriable)(fa: F[A]): F[A]

object Retry:
  def apply[F[_]: Retry]: Retry[F] =
    implicitly

  implicit def forLoggerTemporal[F[_]: Logger: Temporal]: Retry[F] =
    new Retry[F]:
      def retry[A](policy: RetryPolicy[F, Throwable], retriable: Retriable)(fa: F[A]): F[A] =
        def onError(e: Throwable, details: RetryDetails): F[HandlerDecision[F[A]]] =
          details.nextStepIfUnsuccessful match
            case NextStep.DelayAndRetry(nextDelay) =>
              Logger[F].error(
                s"Failed to process ${retriable.show} with ${e.getMessage}. So far we have retried ${details.retriesSoFar} times."
              ) >> Temporal[F].pure(HandlerDecision.Continue)
            case NextStep.GiveUp =>
              Logger[F].error(
                s"Giving up on ${retriable.show} after ${details.retriesSoFar} retries."
              ) >> Temporal[F].pure(HandlerDecision.Stop)
        retryingOnErrors(fa)(policy, onError)
