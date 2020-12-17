package no.oslokommune.ombruk.partner.model

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Partner(
        @field:Schema(description = "ID of partner", minimum = "1", nullable = false)
        val id: Int,
        @field:Schema(description = "The name of a partner", nullable = false)
        var navn: String,
        @field:Schema(description = "Describes the partner", nullable = false)
        var beskrivelse: String,
        @field:Schema(description = "The phone number of a partner", nullable = false)
        var telefon: String,
        @field:Schema(description = "The e-mail of a partner", nullable = false)
        var epost: String,
        @field:Schema(description = "The time of which a partner was updated. If null, partner is original", nullable = true, type = "DateTime")
        @Serializable(with = LocalDateTimeSerializer::class)
        val endretTidspunkt: LocalDateTime? = null,
        @field:Schema(description = "The time of which a partner was deleted. If null, partner exists", nullable = true, type = "DateTime")
        @Serializable(with = LocalDateTimeSerializer::class)
        val slettetTidspunkt: LocalDateTime? = null
)