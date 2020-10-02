package no.oslokommune.ombruk.uttak.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Uttak(
   val id: Int,
   @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime,
   @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime,
   val stasjon: Stasjon,
   val partner: Partner?, // Optional partner. An uttak without a partner is arranged by the stasjon only.
   var gjentakelsesRegel: GjentakelsesRegel? = null,
   val type: UttaksType = UttaksType.GJENTAKENDE,
   val description: String? = null
)

/*
@Serializable
enum class UttaksType(val type: String) {
   ENKELT("ENKELT"),
   GJENTAKENDE("GJENTAKENDE"),
   EKSTRA("EKSTRA"),
   OMBRUKSDAG("OMBRUKSDAG")
}
*/
@Serializable
enum class UttaksType {
   ENKELT,
   GJENTAKENDE,
   EKSTRA,
   OMBRUKSDAG
}
