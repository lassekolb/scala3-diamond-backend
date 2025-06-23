package dev.z
package accepto
package users
package auth

import io.circe.*
import neotype.interop.circe.given

import dev.z.accepto.organizations.CirceCodecs.given

object CirceCodecs:
  given Codec[UserId] =
    Codec.from(
      Decoder[UUID].map(UserId.apply),
      Encoder[UUID].contramap(_.unwrap),
    )
  given Codec[UserName] =
    Codec.from(
      Decoder[NonEmptyString].map(UserName.apply),
      Encoder[NonEmptyString].contramap(_.unwrap),
    )

  given Codec[User] =
    Codec.AsObject.derived

  given Codec[UserSession] =
    Codec.AsObject.derived

  given Codec[Membership] =
    Codec.AsObject.derived

  given Codec[MembershipRole] =
    Codec.from(
      Decoder[String].emap {
        case "admin" => Right(MembershipRole.Admin)
        case "member" => Right(MembershipRole.Member)
        case other => Left(s"Invalid role: $other")
      },
      Encoder[String].contramap {
        case MembershipRole.Admin => "admin"
        case MembershipRole.Member => "member"
      },
    )
