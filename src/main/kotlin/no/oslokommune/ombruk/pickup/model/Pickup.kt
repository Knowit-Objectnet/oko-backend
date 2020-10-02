package no.oslokommune.ombruk.pickup.model

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Pickup(
        val id: Int,
        @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime,
        @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime,
        val description: String? = null,
        val stasjon: Stasjon,
        val chosenPartner: Partner? = null  // if not null, the no.oslokommune.ombruk.pickup has been "fulfilled".
)
