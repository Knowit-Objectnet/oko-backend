package no.oslokommune.ombruk.uttaksdata.model

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Uttaksdata(
    @field:Schema(description = "The ID of the Uttaksdata") val id: Int = 0,
    @field:Schema(description = "The ID of the corresponding Uttak") val uttakId: Int,
    @field:Schema(description = "The weight of an Uttak") val vekt: Int? = null,
    @field:Schema(
        description = "The time of when the Uttaksdata was last updated"
    ) @Serializable(with = LocalDateTimeSerializer::class) val rapportertTidspunkt: LocalDateTime? = null
)