package dev.z
package accepto
package users
package auth
package middleware

import java.util.UUID

import neotype.*
import neotype.common.NonEmptyString

type AdminUserTokenConfig = AdminUserTokenConfig.Type
object AdminUserTokenConfig extends Newtype[NonEmptyString]

type JwtSecretKeyConfig = JwtSecretKeyConfig.Type
object JwtSecretKeyConfig extends Newtype[NonEmptyString]

type ClaimContent = ClaimContent.Type
object ClaimContent extends Subtype[UUID]
