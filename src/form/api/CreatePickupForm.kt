package ombruk.backend.form.api

import kotlinx.serialization.Serializable
import ombruk.backend.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime


@Serializable
data class CreatePickupForm (
    @Serializable(with = LocalDateTimeSerializer::class) var startTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) var endTime: LocalDateTime,
    val stationId: Int
)
