package ombruk.backend.calendar.form

import kotlinx.serialization.Serializable
import ombruk.backend.calendar.model.RecurrenceRule
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.calendar.utils.CreateEventFormIterator
import ombruk.backend.calendar.utils.NonRecurringCreateEventFormIterator
import java.time.LocalDateTime


@Serializable
data class CreateEventForm(
    @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime,
    val stationId: Int,
    val partnerId: Int,
    var recurrenceRule: RecurrenceRule? = null
) : Iterable<CreateEventForm> {
    override fun iterator() = when(recurrenceRule) {
        null -> NonRecurringCreateEventFormIterator(this)
        else -> CreateEventFormIterator(this)
    }
}
