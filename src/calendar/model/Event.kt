package ombruk.backend.calendar.model

import kotlinx.serialization.Serializable
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Event(
    val id: Int,
    @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime,
    val station: Station,
    val partner: Partner?,
    var recurrenceRule: RecurrenceRule? = null
)
