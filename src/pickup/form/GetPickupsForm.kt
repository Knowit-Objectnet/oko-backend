package ombruk.backend.pickup.form

import kotlinx.serialization.Serializable
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class GetPickupsForm (
    @Serializable(with = LocalDateTimeSerializer::class) var startTime: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) var endTime: LocalDateTime? = null,
    val stationId: Int? = null
)

