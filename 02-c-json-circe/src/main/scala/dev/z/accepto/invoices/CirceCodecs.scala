package dev.z
package accepto
package invoices

import io.circe.*
import neotype.interop.circe.given
import squants.market.*

import dev.z.accepto.organizations.CirceCodecs.given

object CirceCodecs:
  // Custom codec for Money
  given Codec[Money] =
    Codec.from(
      Decoder.instance { c =>
        for
          amount <- c.get[BigDecimal]("amount")
          currency <-
            c.get[String]("currency").map {
              case "USD" => USD
              case "EUR" => EUR
              case other => throw new Exception(s"Unsupported currency: $other")
            }
        yield Money(amount, currency)
      },
      Encoder.instance { money =>
        import io.circe.syntax.*
        import io.circe.Json

        Json.obj(
          "amount" -> money.amount.asJson,
          "currency" -> money.currency.code.asJson,
        )
      },
    )

  given Codec[InvoiceId] =
    Codec
      .from(
        Decoder.decodeUUID.map(InvoiceId.apply),
        Encoder.encodeUUID.contramap(_.unwrap),
      )
  given Codec[InvoiceDate] =
    Codec.from(
      Decoder.decodeLocalDate.map(InvoiceDate.apply),
      Encoder.encodeLocalDate.contramap(_.unwrap),
    )

  given Codec[Invoice] =
    Codec.AsObject.derived

  given Codec[CreateInvoice] =
    Codec.AsObject.derived

  given listCodec[T](
    using
    Codec[T]
  ): Codec[List[T]] =
    Codec.from(
      Decoder.decodeList[T],
      Encoder.encodeList[T],
    )
