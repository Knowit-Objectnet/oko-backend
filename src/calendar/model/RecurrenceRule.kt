package ombruk.backend.calendar.model

import kotlinx.serialization.Serializable
import ombruk.backend.shared.model.LocalDateTimeSerializer
import java.time.DayOfWeek
import java.time.LocalDateTime

@Serializable
data class RecurrenceRule(
    var id: Int = 0,
    @Serializable(with = LocalDateTimeSerializer::class) val until: LocalDateTime? = null,
    val days: List<DayOfWeek>? = null,
    val interval: Int = 1,
    val count: Int? = null
)

fun String.toWeekDayList() = this.split(", ").map { DayOfWeek.valueOf(it) }