package ombruk.backend.pickup.form

import kotlinx.serialization.Serializable
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime


@Serializable
data class CreatePickupForm (
    @Serializable(with = LocalDateTimeSerializer::class) var startTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) var endTime: LocalDateTime,
    val stationId: Int
)
