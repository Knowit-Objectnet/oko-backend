package no.oslokommune.ombruk.reporting.model

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Report(
        val reportId: Int = 0,
        val uttakId: Int,
        val partnerId: Int?,
        val stasjon: Stasjon,
        @Serializable(with = LocalDateTimeSerializer::class) val startDateTime: LocalDateTime,
        @Serializable(with = LocalDateTimeSerializer::class) val endDateTime: LocalDateTime,
        val weight: Int? = null,
        @Serializable(with = LocalDateTimeSerializer::class) val reportedDateTime: LocalDateTime? = null
)