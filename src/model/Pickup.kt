package ombruk.backend.model

import kotlinx.serialization.Serializable
import ombruk.backend.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Pickup(
    val id: Int,
    @Serializable(with = LocalDateTimeSerializer::class) var startTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) var endTime: LocalDateTime,
    val station: Station
)
