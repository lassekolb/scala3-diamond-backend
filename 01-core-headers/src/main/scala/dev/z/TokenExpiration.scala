package dev.z

import scala.concurrent.duration.FiniteDuration

import neotype.*

// TokenExpiration as a newtype wrapping FiniteDuration
type TokenExpiration = TokenExpiration.Type
object TokenExpiration extends Newtype[FiniteDuration]
