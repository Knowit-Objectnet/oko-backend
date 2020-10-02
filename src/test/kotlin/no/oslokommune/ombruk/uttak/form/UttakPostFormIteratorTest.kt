package no.oslokommune.ombruk.uttak.form

import no.oslokommune.ombruk.uttak.model.RecurrenceRule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class UttakPostFormIteratorTest {

    @Test
    fun testEverydayStartingSameAsRecurrenceRuleDay() {
        val recurrenceRule = RecurrenceRule(count = 1, days = everyday())
        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            recurrenceRule

        )

        // Iterate over recurringForm and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startDateTime.plusDays(counter),
                recurringForm.endDateTime.plusDays(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(7, counter)
    }

    @Test
    fun testSkipWeekendStartingSameAsRecurrenceRuleDay() {
        val recurrenceRule = RecurrenceRule(count = 1, days = everyWeekDay())

        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            recurrenceRule

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startDateTime.plusDays(counter),
                recurringForm.endDateTime.plusDays(counter)
            )

            assertEquals(expectedForm, actualForm)
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

        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            recurrenceRule

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        var offset = 0L

        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startDateTime.plusDays(counter + offset),
                recurringForm.endDateTime.plusDays(counter + offset)
            )

            assertEquals(expectedForm, actualForm)
            counter++
            if (counter == 2L) offset++
        }

        assertEquals(4, counter)
    }

    @Test
    fun testFormStartingLaterThanDayFromRecurrenceRule() {
        val recurrenceRule = RecurrenceRule(count = 1, days = everyday())

        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-14T15:00:00"),
            LocalDateTime.parse("2020-07-14T15:00:00"),
            1,
            1,
            recurrenceRule

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startDateTime.plusDays(counter),
                recurringForm.endDateTime.plusDays(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(6, counter)
    }

    @Test
    fun testFormBeforeLaterThanDayFromRecurrenceRule() {
        val recurrenceRule = RecurrenceRule(count = 1, days = everyWeekDay())

        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-12T15:00:00"),
            LocalDateTime.parse("2020-07-12T15:00:00"),
            1,
            1,
            recurrenceRule

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startDateTime.plusDays(counter + 1),
                recurringForm.endDateTime.plusDays(counter + 1)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(5, counter)
    }

    @Test
    fun testHighCount() {
        val recurrenceRule = RecurrenceRule(count = 7, days = everyday())

        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            recurrenceRule

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startDateTime.plusDays(counter),
                recurringForm.endDateTime.plusDays(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(49, counter)
    }

    @Test
    fun testFormWithRecurrenceRuleWithoutDays() {
        val recurrenceRule = RecurrenceRule(count = 8)
        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            recurrenceRule

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startDateTime.plusWeeks(counter),
                recurringForm.endDateTime.plusWeeks(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(8, counter)
    }

    @Test
    fun testFormEveryDayWithUntil() {
        val recurrenceRule = RecurrenceRule(
            until = LocalDateTime.parse("2020-07-30T15:00:00"),
            days = everyday()
        )
        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            recurrenceRule

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startDateTime.plusDays(counter),
                recurringForm.endDateTime.plusDays(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(18, counter)
    }


    @Test
    fun testFormWeekdayWithUntil() {
        val recurrenceRule = RecurrenceRule(
            until = LocalDateTime.parse("2020-07-20T15:00:00"),
            days = everyWeekDay()
        )


        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            recurrenceRule

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        var offset = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startDateTime.plusDays(counter + offset),
                recurringForm.endDateTime.plusDays(counter + offset)
            )

            assertEquals(expectedForm, actualForm)
            counter++
            if (counter == 5L) offset += 2
        }
        assertEquals(6, counter)
    }

    @Test
    fun testFormWeekdaySkipWednesdayWithUntil() {
        val recurrenceRule = RecurrenceRule(
            until = LocalDateTime.parse("2020-07-20T15:00:00"),
            days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        )

        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            recurrenceRule

        )


        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        var offset = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startDateTime.plusDays(counter + offset),
                recurringForm.endDateTime.plusDays(counter + offset)
            )

            assertEquals(expectedForm, actualForm)
            counter++
            if (counter + offset == 5L) offset += 2
            if (counter + offset == 2L) offset++
        }
        assertEquals(5, counter)
    }

    @Test
    fun testFormWithoutRecurrenceRule() {

        val nonRecurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-12T15:00:00"),
            LocalDateTime.parse("2020-07-12T15:00:00"),
            1,
            1
        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in nonRecurringForm) {
            val expectedForm = nonRecurringForm.copy(
                nonRecurringForm.startDateTime.plusDays(counter),
                nonRecurringForm.endDateTime.plusDays(counter)
            )
            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(1, counter)
    }

    private fun everyWeekDay() = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    )

    private fun everyday() = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )


}
