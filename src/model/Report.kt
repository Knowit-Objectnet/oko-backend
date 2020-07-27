package ombruk.backend.model

import kotlinx.serialization.Serializable
import ombruk.backend.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Report(
    val reportID: Int = 0,
    val eventId: Int = 0,
    val partner: Partner,
    val weight: Int,
    @Serializable(with = LocalDateTimeSerializer::class) val createdDateTime: LocalDateTime = LocalDateTime.now()
)