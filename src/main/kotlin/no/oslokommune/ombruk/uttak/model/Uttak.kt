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
    @Serializable(with = LocalDateTimeSerializer::class) val startTidspunkt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val sluttTidspunkt: LocalDateTime,
    val stasjon: Stasjon,
    val partner: Partner?, // Optional partner. An uttak without a partner is arranged by the stasjon only.
    var gjentakelsesRegel: GjentakelsesRegel? = null,
    val type: UttaksType = UttaksType.GJENTAKENDE,
    val beskrivelse: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val endretTidspunkt: LocalDateTime? = null
)

@Serializable
enum class UttaksType {
   ENKELT,
   GJENTAKENDE,
   EKSTRA,
   OMBRUKSDAG
}
