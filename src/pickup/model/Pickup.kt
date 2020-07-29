package ombruk.backend.pickup.model

import kotlinx.serialization.Serializable
import ombruk.backend.calendar.model.Station
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Pickup(
    val id: Int,
    @Serializable(with = LocalDateTimeSerializer::class) var startTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) var endTime: LocalDateTime,
    val station: Station
)
