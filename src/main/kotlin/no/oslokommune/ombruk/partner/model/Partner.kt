package no.oslokommune.ombruk.partner.model

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Partner(
        val id: Int,
        var navn: String,
        var beskrivelse: String,
        var telefon: String,
        var epost: String,
        @Serializable(with = LocalDateTimeSerializer::class)
        val endretTidspunkt: LocalDateTime? = null,
        @Serializable(with = LocalDateTimeSerializer::class)
        val slettetTidspunkt: LocalDateTime? = null
)