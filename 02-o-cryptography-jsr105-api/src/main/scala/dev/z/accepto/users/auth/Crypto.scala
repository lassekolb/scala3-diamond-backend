package dev.z
package accepto
package users
package auth

import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.*
import javax.crypto.spec.*

import cats.effect.Sync
import cats.syntax.all.*

object CryptoImpl:
  private val random =
    new SecureRandom()
  def make[F[_]: Sync](passwordSalt: PasswordSalt): F[Crypto[F]] =
    Sync[F]
      .delay(deriveKey(passwordSalt))
      .map:
        secretKey =>
          new Crypto:
            override def encrypt(password: Password): F[EncryptedPassword] =
              Sync[F].delay:
                val ivBytes = new Array[Byte](16)
                random.nextBytes(ivBytes)
                val iv = new IvParameterSpec(ivBytes)
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
                val bytes = password.unwrap.getBytes(StandardCharsets.UTF_8)
                val encryptedBytes = cipher.doFinal(bytes)
                val combined = ivBytes ++ encryptedBytes
                val result = Base64.getEncoder().encodeToString(combined)
                EncryptedPassword(result)
            private def decrypt(password: EncryptedPassword): F[Option[Password]] =
              Sync[F]
                .attempt(Sync[F].delay {
                  val decoded = Base64.getDecoder.decode(password.unwrap)

                  // Extract IV and ciphertext
                  val ivBytes = decoded.take(16)
                  val ciphertext = decoded.drop(16)
                  val iv = new IvParameterSpec(ivBytes)

                  // Create and initialize cipher for decryption
                  val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                  cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)

                  val decryptedBytes = cipher.doFinal(ciphertext)
                  val result = new String(decryptedBytes, StandardCharsets.UTF_8)
                  Password(result)
                })
                .map(_.toOption) // Convert errors to `None`

            // private def decrypt(password: EncryptedPassword): Password =
            //   val decoded = Base64.getDecoder().decode(password.unwrap)
            //
            //   // Extract IV and ciphertext
            //   val ivBytes = decoded.take(16)
            //   val ciphertext = decoded.drop(16)
            //   val iv = new IvParameterSpec(ivBytes)
            //
            //   // Create and initialize cipher for decryption
            //   val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            //   cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
            //
            //   val decryptedBytes = cipher.doFinal(ciphertext)
            //   val result = new String(decryptedBytes, StandardCharsets.UTF_8)
            //   Password(result)
            override def verifyPassword(provided: Password, stored: EncryptedPassword): F[Boolean] =
              decrypt(stored).map(_.contains(provided))
            // override def verifyPassword(provided: Password, stored: EncryptedPassword): F[Boolean] =
            //   Sync[F].delay {
            //     try decrypt(stored) == provided
            //     catch case _: Exception => false
            //   }

  private def deriveKey(passwordSalt: PasswordSalt): SecretKeySpec =
    val salt = passwordSalt.unwrap.getBytes(StandardCharsets.UTF_8)
    val keySpec = new PBEKeySpec("password".toCharArray(), salt, 65536, 256)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val bytes = factory.generateSecret(keySpec).getEncoded
    new SecretKeySpec(bytes, "AES")

  type EncryptCipher =
    EncryptCipher.Type
  object EncryptCipher extends Subtype[Cipher]
  type DecryptCipher =
    DecryptCipher.Type
  object DecryptCipher extends Subtype[Cipher]
