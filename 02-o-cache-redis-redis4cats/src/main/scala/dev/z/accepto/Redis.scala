package dev.z
package accepto

type RedisUriString = RedisUriString.Type
object RedisUriString extends Subtype[String]:
  inline override def validate(input: String): Boolean | String =
    if input.trim.isEmpty then "URI cannot be empty"
    else if !input.startsWith("redis://") then "URI must start with redis://"
    else true

object Redis:
  case class Config(uri: Config.URI)
  object Config:
    type URI =
      URI.Type
    object URI extends Subtype[RedisUriString]
