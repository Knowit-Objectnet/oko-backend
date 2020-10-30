package no.oslokommune.ombruk.uttak.form

import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class UttakPostFormIteratorTest {

    @Test
    fun testEverydayStartingSameAsGjentakelsesRegelDay() {
        val gjentakelsesRegel = GjentakelsesRegel(count = 1, days = everyday())
        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            gjentakelsesRegel

        )

        // Iterate over recurringForm and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startTidspunkt.plusDays(counter),
                recurringForm.sluttTidspunkt.plusDays(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(7, counter)
    }

    @Test
    fun testSkipWeekendStartingSameAsGjentakelsesRegelDay() {
        val gjentakelsesRegel = GjentakelsesRegel(count = 1, days = everyWeekDay())

        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startTidspunkt.plusDays(counter),
                recurringForm.sluttTidspunkt.plusDays(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(5, counter)
    }

    @Test
    fun testSkipWednesdayStartingSameAsGjentakelsesRegelDay() {
        val gjentakelsesRegel = GjentakelsesRegel(
            count = 1,
            days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        )

        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        var offset = 0L

        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startTidspunkt.plusDays(counter + offset),
                recurringForm.sluttTidspunkt.plusDays(counter + offset)
            )

            assertEquals(expectedForm, actualForm)
            counter++
            if (counter == 2L) offset++
        }

        assertEquals(4, counter)
    }

    @Test
    fun testFormStartingLaterThanDayFromGjentakelsesRegel() {
        val gjentakelsesRegel = GjentakelsesRegel(count = 1, days = everyday())

        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-14T15:00:00"),
            LocalDateTime.parse("2020-07-14T15:00:00"),
            1,
            1,
            gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startTidspunkt.plusDays(counter),
                recurringForm.sluttTidspunkt.plusDays(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(6, counter)
    }

    @Test
    fun testFormBeforeLaterThanDayFromGjentakelsesRegel() {
        val gjentakelsesRegel = GjentakelsesRegel(count = 1, days = everyWeekDay())

        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-12T15:00:00"),
            LocalDateTime.parse("2020-07-12T15:00:00"),
            1,
            1,
            gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startTidspunkt.plusDays(counter + 1),
                recurringForm.sluttTidspunkt.plusDays(counter + 1)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(5, counter)
    }

    @Test
    fun testHighCount() {
        val gjentakelsesRegel = GjentakelsesRegel(count = 7, days = everyday())

        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startTidspunkt.plusDays(counter),
                recurringForm.sluttTidspunkt.plusDays(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(49, counter)
    }

    @Test
    fun testFormWithGjentakelsesRegelWithoutDays() {
        val gjentakelsesRegel = GjentakelsesRegel(count = 8)
        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startTidspunkt.plusWeeks(counter),
                recurringForm.sluttTidspunkt.plusWeeks(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(8, counter)
    }

    @Test
    fun testFormEveryDayWithUntil() {
        val gjentakelsesRegel = GjentakelsesRegel(
            until = LocalDateTime.parse("2020-07-30T15:00:00"),
            days = everyday()
        )
        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startTidspunkt.plusDays(counter),
                recurringForm.sluttTidspunkt.plusDays(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(18, counter)
    }


    @Test
    fun testFormWeekdayWithUntil() {
        val gjentakelsesRegel = GjentakelsesRegel(
            until = LocalDateTime.parse("2020-07-20T15:00:00"),
            days = everyWeekDay()
        )


        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        var offset = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startTidspunkt.plusDays(counter + offset),
                recurringForm.sluttTidspunkt.plusDays(counter + offset)
            )

            assertEquals(expectedForm, actualForm)
            counter++
            if (counter == 5L) offset += 2
        }
        assertEquals(6, counter)
    }

    @Test
    fun testFormWeekdaySkipWednesdayWithUntil() {
        val gjentakelsesRegel = GjentakelsesRegel(
            until = LocalDateTime.parse("2020-07-20T15:00:00"),
            days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        )

        val recurringForm = UttakPostForm(
            LocalDateTime.parse("2020-07-13T15:00:00"),
            LocalDateTime.parse("2020-07-13T15:00:00"),
            1,
            1,
            gjentakelsesRegel

        )


        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        var offset = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                recurringForm.startTidspunkt.plusDays(counter + offset),
                recurringForm.sluttTidspunkt.plusDays(counter + offset)
            )

            assertEquals(expectedForm, actualForm)
            counter++
            if (counter + offset == 5L) offset += 2
            if (counter + offset == 2L) offset++
        }
        assertEquals(5, counter)
    }

    @Test
    fun testFormWithoutGjentakelsesRegel() {

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
                nonRecurringForm.startTidspunkt.plusDays(counter),
                nonRecurringForm.sluttTidspunkt.plusDays(counter)
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
