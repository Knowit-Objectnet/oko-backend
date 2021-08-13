package ombruk.backend.shared.utils

import ombruk.backend.henting.domain.model.HenteplanFrekvens
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


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

class LocalDateTimeProgressionWithDayFrekvens(
    override val start: LocalDateTime,
    override val endInclusive: LocalDateTime,
    private val dayOfWeek: DayOfWeek?, //Should be null only for ENKELT
    private val frekvens: HenteplanFrekvens
) :
    Iterable<LocalDateTime>, ClosedRange<LocalDateTime> {

    fun findFirstDateOfDay(): LocalDateTime{
        if (frekvens == HenteplanFrekvens.ENKELT) return start
        val dayDifference = dayOfWeek!!.value - start.dayOfWeek.value
        return start.plusDays(((dayDifference + 7)%7).toLong())
    }

    fun findStepFromFrekvens(): Int {
        return when(frekvens) {
            HenteplanFrekvens.ENKELT -> 1
            HenteplanFrekvens.UKENTLIG -> 7
            HenteplanFrekvens.ANNENHVER -> 14
        }
    }

    fun createIterator() : Iterator<LocalDateTime> {
        if (frekvens == HenteplanFrekvens.ENKELT) return LocalDateTimeIterator(start, start, 1)
        else return LocalDateTimeIterator(findFirstDateOfDay(), endInclusive, findStepFromFrekvens().toLong())
    }

    override fun iterator(): Iterator<LocalDateTime> = createIterator()
}

/**
 * Extension function to [LocalDateTime] so that we can create a range over the progression.
 */
operator fun LocalDateTime.rangeTo(other: LocalDateTime) =
    LocalDateTimeProgression(this, other)

fun formatDateRange(start: LocalDateTime, end: LocalDateTime): String {
    val dateformatter = DateTimeFormatter.ofPattern("dd.MM.yy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    return "${ start.format(dateformatter) } kl ${start.format(timeFormatter)}-${end.format(timeFormatter)}"
}

fun formatDateTime(dateTime: LocalDateTime): String {
    val dateformatter = DateTimeFormatter.ofPattern("dd.MM.yy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    return "${ dateTime.format(dateformatter) } kl ${dateTime.format(timeFormatter)}"
}