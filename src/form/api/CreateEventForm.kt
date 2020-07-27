package ombruk.backend.form.api

import kotlinx.serialization.Serializable
import ombruk.backend.model.RecurrenceRule
import ombruk.backend.model.serializer.LocalDateTimeSerializer
import ombruk.backend.utils.CreateEventFormIterator
import ombruk.backend.utils.NonRecurringCreateEventFormIterator
import java.time.LocalDateTime


@Serializable
data class CreateEventForm(
    val id: Int = 0,
    @Serializable(with = LocalDateTimeSerializer::class) var startDateTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) var endDateTime: LocalDateTime,
    val stationId: Int,
    val partnerId: Int,
    var recurrenceRule: RecurrenceRule? = null
) : Iterable<CreateEventForm> {
    override fun iterator() = when(recurrenceRule) {
        null -> NonRecurringCreateEventFormIterator(this)
        else ->  CreateEventFormIterator(this)
    }
}
