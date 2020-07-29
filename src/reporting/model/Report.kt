package ombruk.backend.reporting.model

import kotlinx.serialization.Serializable
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Report(
    val reportID: Int = 0,
    val eventID: Int,
    val partnerID: Int,
    val stationID: Int,
    @Serializable(with = LocalDateTimeSerializer::class) val startDateTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val endDateTime: LocalDateTime,
    val weight: Int?,
    @Serializable(with = LocalDateTimeSerializer::class) val reportedDateTime: LocalDateTime? = null
)