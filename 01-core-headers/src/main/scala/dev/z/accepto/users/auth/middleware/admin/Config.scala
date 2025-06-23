package dev.z
package accepto
package users
package auth
package middleware
package admin

final case class Config(tokenKey: JwtSecretKeyConfig, adminKey: AdminUserTokenConfig)
