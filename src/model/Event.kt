package ombruk.backend.model

import kotlinx.serialization.Serializable
import ombruk.backend.model.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime


enum class EventType { SINGLE, RECURRING }

@Serializable
data class Event(
    val id: Int = 0,
    @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime,
    val station: Station,
    val partner: Partner,
    var recurrenceRule: RecurrenceRule? = null
)
