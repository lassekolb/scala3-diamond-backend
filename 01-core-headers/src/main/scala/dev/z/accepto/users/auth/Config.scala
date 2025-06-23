package dev.z
package accepto
package users
package auth

final case class Config(
  tokenExpiration: TokenExpiration,
  jwtAccessTokenKeyConfig: JwtAccessTokenKeyConfig,
  passwordSalt: PasswordSalt,
)
