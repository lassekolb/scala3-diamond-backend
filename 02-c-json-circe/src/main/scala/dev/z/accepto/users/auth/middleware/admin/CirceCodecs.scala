package dev.z
package accepto
package users
package auth
package middleware
package admin

import io.circe.Decoder

object CirceCodecs:
  given Decoder[ClaimContent] =
    Decoder.forProduct1("uuid")(ClaimContent.apply)
