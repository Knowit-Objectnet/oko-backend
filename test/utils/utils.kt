package ombruk.backend.utils

import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.RecurrenceRule
import java.time.DayOfWeek
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

fun assertEventEqual(expected: Event, actual: Event) {
    assertEquals(expected.id, actual.id)
    assertEquals(expected.startDateTime, actual.startDateTime)
    assertEquals(expected.endDateTime, actual.endDateTime)
    assertEquals(expected.partner, actual.partner)
    assertEquals(expected.station, actual.station)

    expected.recurrenceRule?.let { expectedRecurrenceRule ->
        assertNotNull(actual.recurrenceRule) { actualRecurrenceRule ->
            assertRecurrenceRuleEqual(expectedRecurrenceRule, actualRecurrenceRule)
        }
    } ?: assertNull(actual.recurrenceRule)
}

fun assertRecurrenceRuleEqual(expected: RecurrenceRule, actual: RecurrenceRule) {
    assertEquals(expected.id, actual.id)
    assertEquals(expected.count, actual.count)
    assertEquals(expected.until, actual.until)
    assertEquals(expected.interval, actual.interval)
    assertEquals(expected.days, actual.days)
}

fun everyWeekDay() = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY
)

fun everyday() = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY
)
