package ombruk.backend.shared.utils

import java.time.LocalDateTime


/**
 * Class for iterating over a given range of dates. The stepDays is how many days each iteration should move forward.
 * @param startDate a [LocalDateTime] to start at
 * @param endDateInclusive a [LocalDateTime] to end at. Note that this is inclusive
 * @param stepDays how many days to move at each iteration
 */
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

/**
 * A progression over LocalDateTime
 * @param start a [LocalDateTime] that starts the progression
 * @param endInclusive a [LocalDateTime] that ends the progression. Note that this is inclusive
 * @param stepDays how many days to move at each step
 */
class LocalDateTimeProgression(
    override val start: LocalDateTime,
    override val endInclusive: LocalDateTime,
    private val stepDays: Long = 1
) :
    Iterable<LocalDateTime>, ClosedRange<LocalDateTime> {

    override fun iterator(): Iterator<LocalDateTime> =
        LocalDateTimeIterator(start, endInclusive, stepDays)
}

/**
 * Extension function to [LocalDateTime] so that we can create a range over the progression.
 */
operator fun LocalDateTime.rangeTo(other: LocalDateTime) =
    LocalDateTimeProgression(this, other)