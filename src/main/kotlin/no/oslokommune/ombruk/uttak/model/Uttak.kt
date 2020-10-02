package no.oslokommune.ombruk.uttak.model

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
        var recurrenceRule: RecurrenceRule? = null
)
