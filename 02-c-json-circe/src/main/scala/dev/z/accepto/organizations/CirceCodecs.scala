package dev.z
package accepto
package organizations

import io.circe.*

object CirceCodecs:
  given Codec[OrganizationId] =
    Codec.from(
      Decoder[UUID].emap(OrganizationId.make),
      Encoder[UUID].contramap(_.unwrap),
    )

  given Codec[OrganizationName] =
    Codec.from(
      Decoder[String].emap(OrganizationName.make),
      Encoder[String].contramap(_.unwrap),
    )
  given Codec[Organization] =
    Codec.AsObject.derived
