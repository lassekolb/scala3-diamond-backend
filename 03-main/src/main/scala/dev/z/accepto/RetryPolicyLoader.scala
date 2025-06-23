package dev.z
package accepto

import scala.concurrent.duration.*

import cats.*
import cats.syntax.all.*
import retry.*

object RetryPolicyLoader:
  def load[F[_]: Applicative]: F[RetryPolicy[F, Throwable]] =
    List(
      RetryPolicies.limitRetries(3),
      RetryPolicies.exponentialBackoff(10.milliseconds),
    ).reduceLeft(_.combine(_)).pure
