package ombruk.backend.reporting.model

import kotlinx.serialization.Serializable
import ombruk.backend.calendar.model.Station
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Report(
    val reportId: Int = 0,
    val eventId: Int,
    val partnerId: Int?,
    val station: Station,
    @Serializable(with = LocalDateTimeSerializer::class) val startDateTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val endDateTime: LocalDateTime,
    val weight: Int? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val reportedDateTime: LocalDateTime? = null
)