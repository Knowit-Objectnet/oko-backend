package ombruk.backend

import ombruk.backend.model.Event
import ombruk.backend.model.Partner
import ombruk.backend.model.RecurrenceRule
import ombruk.backend.model.Station
import ombruk.backend.utils.assertEventEqual
import ombruk.backend.utils.everyWeekDay
import ombruk.backend.utils.everyday
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFails

class RecurringEventIteratorTest {

    companion object {
        val testPartner = Partner(10, "TestPartner 1")
        val testStation = Station(10, "TestStation 1")
    }

    @Test
    fun testEverydayStartingSameAsRecurrenceRuleDay() {
        val recurrenceRule = RecurrenceRule(count = 1, days = everyday())
        val recurringEvent = Event(
            0,
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            testStation,
            testPartner,
            recurrenceRule

        )

        // Iterate over recurringEvent and check that it matches the expected event.
        var counter = 0L
        for (event in recurringEvent) {
            val expectedEvent = Event(
                0,
                recurringEvent.startDateTime.plusDays(counter),
                recurringEvent.endDateTime.plusDays(counter),
                testStation,
                testPartner,
                recurrenceRule
            )
            assertEventEqual(expectedEvent, event)
            counter++
        }
        assertEquals(7, counter)
    }

    @Test
    fun testSkipWeekendStartingSameAsRecurrenceRuleDay() {
        val recurrenceRule = RecurrenceRule(count = 1, days = everyWeekDay())

        val recurringEvent = Event(
            0,
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            testStation,
            testPartner,
            recurrenceRule
        )

        // Iterate over recurringEvent and check that it matches the expected event.
        var counter = 0L
        for (event in recurringEvent) {
            val expectedEvent = Event(
                0,
                recurringEvent.startDateTime.plusDays(counter),
                recurringEvent.endDateTime.plusDays(counter),
                testStation,
                testPartner,
                recurrenceRule
            )
            assertEventEqual(expectedEvent, event)
            counter++
        }
        assertEquals(5, counter)
    }

    @Test
    fun testSkipWednesdayStartingSameAsRecurrenceRuleDay() {
        val recurrenceRule = RecurrenceRule(
            count = 1,
            days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        )
        val recurringEvent = Event(
            0,
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            testStation,
            testPartner,
            recurrenceRule
        )

        // Iterate over recurringEvent and check that it matches the expected event.
        var counter = 0L
        var offset = 0L
        for (event in recurringEvent) {
            val expectedEvent = Event(
                0,
                recurringEvent.startDateTime.plusDays(counter + offset),
                recurringEvent.endDateTime.plusDays(counter + offset),
                testStation,
                testPartner,
                recurrenceRule
            )
            assertEventEqual(expectedEvent, event)
            counter++
            if (counter == 2L) offset++
        }
        assertEquals(4, counter)
    }

    @Test
    fun testEventStartingLaterThanDayFromRecurrenceRule() {
        val recurrenceRule = RecurrenceRule(count = 1, days = everyday())
        val recurringEvent = Event(
            0,
            LocalDateTime.parse("2020-07-14T15:00:00"),
            LocalDateTime.parse("2020-07-14T15:00:00"),
            testStation,
            testPartner,
            recurrenceRule
        )

        // Iterate over recurringEvent and check that it matches the expected event.
        var counter = 0L
        for (event in recurringEvent) {
            val expectedEvent = Event(
                0,
                recurringEvent.startDateTime.plusDays(counter),
                recurringEvent.endDateTime.plusDays(counter),
                testStation,
                testPartner,
                recurrenceRule
            )
            assertEventEqual(expectedEvent, event)
            counter++
        }
        assertEquals(6, counter)
    }

    @Test
    fun testEventBeforeLaterThanDayFromRecurrenceRule() {
        val recurrenceRule = RecurrenceRule(count = 1, days = everyWeekDay())
        val recurringEvent = Event(
            0,
            LocalDateTime.parse("2020-07-12T15:00:00"),
            LocalDateTime.parse("2020-07-12T15:00:00"),
            testStation,
            testPartner,
            recurrenceRule
        )

        // Iterate over recurringEvent and check that it matches the expected event.
        var counter = 1L
        for (event in recurringEvent) {
            val expectedEvent = Event(
                0,
                recurringEvent.startDateTime.plusDays(counter),
                recurringEvent.endDateTime.plusDays(counter),
                testStation,
                testPartner,
                recurrenceRule
            )
            assertEventEqual(expectedEvent, event)
            counter++
        }
        assertEquals(6, counter)
    }

    @Test
    fun testHighCount() {
        val recurrenceRule = RecurrenceRule(count = 7, days = everyday())
        val recurringEvent = Event(
            0,
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            testStation,
            testPartner,
            recurrenceRule

        )

        // Iterate over recurringEvent and check that it matches the expected event.
        var counter = 0L
        for (event in recurringEvent) {
            val expectedEvent = Event(
                0,
                recurringEvent.startDateTime.plusDays(counter),
                recurringEvent.endDateTime.plusDays(counter),
                testStation,
                testPartner,
                recurrenceRule
            )
            assertEventEqual(expectedEvent, event)
            counter++
        }
        assertEquals(49, counter)
    }

    @Test
    fun testEventWithoutRecurrenceRuleDays() {
        val recurrenceRule = RecurrenceRule(count = 8)
        val recurringEvent = Event(
            0,
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            testStation,
            testPartner,
            recurrenceRule

        )

        // Iterate over recurringEvent and check that it matches the expected event.
        var counter = 0L
        for (event in recurringEvent) {
            val expectedEvent = Event(
                0,
                recurringEvent.startDateTime.plusWeeks(counter),
                recurringEvent.endDateTime.plusWeeks(counter),
                testStation,
                testPartner,
                recurrenceRule
            )
            assertEventEqual(expectedEvent, event)
            counter++
        }
        assertEquals(8, counter)
    }

    @Test
    fun testEventEveryDayWithUntil() {
        val recurrenceRule = RecurrenceRule(until = LocalDateTime.parse("2020-07-30T15:00:00"), days = everyday())
        val recurringEvent = Event(
            0,
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            testStation,
            testPartner,
            recurrenceRule

        )

        // Iterate over recurringEvent and check that it matches the expected event.
        var counter = 0L
        for (event in recurringEvent) {
            val expectedEvent = Event(
                0,
                recurringEvent.startDateTime.plusDays(counter),
                recurringEvent.endDateTime.plusDays(counter),
                testStation,
                testPartner,
                recurrenceRule
            )
            assertEventEqual(expectedEvent, event)
            counter++
        }
        assertEquals(18, counter)
    }

    @Test
    fun testEventWeekdayWithUntil() {
        val recurrenceRule = RecurrenceRule(until = LocalDateTime.parse("2020-07-20T15:00:00"), days = everyWeekDay())
        val recurringEvent = Event(
            0,
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            testStation,
            testPartner,
            recurrenceRule

        )

        // Iterate over recurringEvent and check that it matches the expected event.
        var counter = 0L
        var offset = 0L
        for (event in recurringEvent) {
            val expectedEvent = Event(
                0,
                recurringEvent.startDateTime.plusDays(counter + offset),
                recurringEvent.endDateTime.plusDays(counter + offset),
                testStation,
                testPartner,
                recurrenceRule
            )
            assertEventEqual(expectedEvent, event)
            counter++
            if (counter == 5L) offset += 2
        }
        assertEquals(6, counter)
    }

    @Test
    fun testEventWeekdaySkipWednesdayWithUntil() {
        val recurrenceRule = RecurrenceRule(
            until = LocalDateTime.parse("2020-07-20T15:00:00"),
            days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        )
        val recurringEvent = Event(
            0,
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            testStation,
            testPartner,
            recurrenceRule

        )

        // Iterate over recurringEvent and check that it matches the expected event.
        var counter = 0L
        var offset = 0L
        for (event in recurringEvent) {
            val expectedEvent = Event(
                0,
                recurringEvent.startDateTime.plusDays(counter + offset),
                recurringEvent.endDateTime.plusDays(counter + offset),
                testStation,
                testPartner,
                recurrenceRule
            )
            assertEventEqual(expectedEvent, event)
            counter++
            if (counter + offset == 5L) offset += 2
            if (counter + offset == 2L) offset++
        }
        assertEquals(5, counter)
    }

    @Test
    fun testEventWithoutRecurrenceRule() {
        val nonRecurringEvent = Event(
            0,
            LocalDateTime.parse("2020-07-12T15:00:00"),
            LocalDateTime.parse("2020-07-12T15:00:00"),
            testStation,
            testPartner
        )

        // Iterate over recurringEvent and check that it matches the expected event.
        var counter = 0L
        for (event in nonRecurringEvent) {
            val expectedEvent = Event(
                0,
                nonRecurringEvent.startDateTime.plusDays(counter),
                nonRecurringEvent.endDateTime.plusDays(counter),
                testStation,
                testPartner
            )
            assertEventEqual(expectedEvent, event)
            counter++
        }
        assertEquals(1, counter)
    }
}