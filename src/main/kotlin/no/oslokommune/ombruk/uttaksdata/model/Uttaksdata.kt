package no.oslokommune.ombruk.uttaksdata.model

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Uttaksdata(
        val uttaksdataId: Int = 0,
        val uttakId: Int,
        val partnerId: Int?,
        val stasjon: Stasjon,
        @Serializable(with = LocalDateTimeSerializer::class) val startDateTime: LocalDateTime,
        @Serializable(with = LocalDateTimeSerializer::class) val endDateTime: LocalDateTime,
        val weight: Int? = null,
        @Serializable(with = LocalDateTimeSerializer::class) val uttaksdataedDateTime: LocalDateTime? = null
)