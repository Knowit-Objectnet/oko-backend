package no.oslokommune.ombruk.uttak.model

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
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