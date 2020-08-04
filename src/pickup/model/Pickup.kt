package ombruk.backend.pickup.model

import kotlinx.serialization.Serializable
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Pickup(
    val id: Int,
    @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime,
    val description: String? = null,
    val station: Station,
    val chosenPartner: Partner? = null  // if not null, the pickup has been "fulfilled".
)
