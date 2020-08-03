package ombruk.backend.pickup.form

import kotlinx.serialization.Serializable
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime


@Serializable
data class PatchPickupForm (
    val id: Int,
    @Serializable(with = LocalDateTimeSerializer::class) var startTime: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) var endTime: LocalDateTime? = null
)