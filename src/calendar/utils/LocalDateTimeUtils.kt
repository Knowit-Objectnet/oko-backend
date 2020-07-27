package ombruk.backend.calendar.utils

import java.time.LocalDateTime


class LocalDateTimeIterator(
    startDate: LocalDateTime,
    private val endDateInclusive: LocalDateTime,
    private val stepDays: Long
) : Iterator<LocalDateTime> {

    private var currentDate = startDate
    override fun hasNext() = currentDate <= endDateInclusive
    override fun next(): LocalDateTime {
        val next = currentDate
        currentDate = currentDate.plusDays(stepDays)
        return next
    }
}

class LocalDateTimeProgression(
    override val start: LocalDateTime,
    override val endInclusive: LocalDateTime,
    private val stepDays: Long = 1
) :
    Iterable<LocalDateTime>, ClosedRange<LocalDateTime> {

    override fun iterator(): Iterator<LocalDateTime> =
        LocalDateTimeIterator(start, endInclusive, stepDays)
}

operator fun LocalDateTime.rangeTo(other: LocalDateTime) =
    LocalDateTimeProgression(this, other)