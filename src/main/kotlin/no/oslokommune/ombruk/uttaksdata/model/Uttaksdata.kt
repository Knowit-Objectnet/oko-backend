package no.oslokommune.ombruk.uttaksdata.model

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Uttaksdata(
        val id: Int = 0,
        val uttakId: Int,
        val vekt: Int,
        @Serializable(with = LocalDateTimeSerializer::class)
        val rapportertTidspunkt: LocalDateTime
)