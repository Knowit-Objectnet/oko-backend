package no.oslokommune.ombruk.uttak.form

import no.oslokommune.ombruk.shared.utils.getNumberOfWorkdaysBetween
import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class UttakPostFormIteratorTest {

    @Test
    fun testEverydayStartingSameAsGjentakelsesRegelDay() {
        val gjentakelsesRegel = GjentakelsesRegel(
            antall = 1,
            dager = everyday())

        val recurringForm = UttakPostForm(
            startTidspunkt = LocalDateTime.parse("2020-07-13T15:00:00"),
            sluttTidspunkt = LocalDateTime.parse("2020-07-13T15:00:00"),
            stasjonId = 1,
            partnerId = 1,
            gjentakelsesRegel = gjentakelsesRegel
        )

        // Iterate over recurringForm and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                startTidspunkt = recurringForm.startTidspunkt.plusDays(counter),
                sluttTidspunkt = recurringForm.sluttTidspunkt.plusDays(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(7, counter)
    }

    @Test
    fun testSkipWeekendStartingSameAsGjentakelsesRegelDay() {
        val gjentakelsesRegel = GjentakelsesRegel(
            antall = 1,
            dager = everyWeekDay(),
            until = LocalDateTime.parse("2020-07-22T17:00:00"))

        val recurringForm = UttakPostForm(
            startTidspunkt = LocalDateTime.parse("2020-07-13T15:00:00"),
            sluttTidspunkt = LocalDateTime.parse("2020-07-13T15:00:00"),
            stasjonId = 1,
            partnerId = 1,
            gjentakelsesRegel = gjentakelsesRegel
        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        val actual = recurringForm.toList()
        val test = mutableListOf<UttakPostForm>()
        var offset = 0
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                startTidspunkt = recurringForm.startTidspunkt.plusDays(counter + offset),
                sluttTidspunkt = recurringForm.sluttTidspunkt.plusDays(counter + offset)
            )

            //assertEquals(expectedForm, actualForm)
            counter++
            if (counter == 5L) offset = 2
            test.add(expectedForm)
        }
        //assertEquals(5, counter)
        assertEquals(test, actual)
    }

    @Test
    fun testSkipWednesdayStartingSameAsGjentakelsesRegelDay() {
        val gjentakelsesRegel = GjentakelsesRegel(
            antall = 1,
            dager = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            until = LocalDateTime.parse("2020-07-19T18:00:00")
        )

        val recurringForm = UttakPostForm(
            startTidspunkt = LocalDateTime.parse("2020-07-13T15:00:00"),
            sluttTidspunkt = LocalDateTime.parse("2020-07-13T16:00:00"),
            partnerId = 1,
            stasjonId = 1,
            gjentakelsesRegel = gjentakelsesRegel
        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        var offset = 0L

        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                startTidspunkt = recurringForm.startTidspunkt.plusDays(counter + offset),
                sluttTidspunkt = recurringForm.sluttTidspunkt.plusDays(counter + offset)
            )

            assertEquals(expectedForm, actualForm)
            counter++
            if (counter == 2L) offset++
        }

        assertEquals(4, counter)
    }

    @Test
    fun testFormStartingLaterThanDayFromGjentakelsesRegel() {
        val gjentakelsesRegel = GjentakelsesRegel(
            antall = 1,
            dager = everyday(),
            until = LocalDateTime.parse("2020-07-19T16:00:00")
        )

        val recurringForm = UttakPostForm(
            startTidspunkt = LocalDateTime.parse("2020-07-14T15:00:00"),
            sluttTidspunkt = LocalDateTime.parse("2020-07-14T15:00:00"),
            stasjonId = 1,
            partnerId = 1,
            gjentakelsesRegel = gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                startTidspunkt = recurringForm.startTidspunkt.plusDays(counter),
                sluttTidspunkt = recurringForm.sluttTidspunkt.plusDays(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(6, counter)
    }

    @Test
    fun testFormBeforeLaterThanDayFromGjentakelsesRegel() {
        val gjentakelsesRegel = GjentakelsesRegel(
            antall = 1,
            dager = everyWeekDay(),
            until = LocalDateTime.parse("2020-07-17T16:00:00")
            //sluttTidspunkt = LocalDateTime.parse("2020-07-14T15:00:00")
        )

        val recurringForm = UttakPostForm(
            startTidspunkt = LocalDateTime.parse("2020-07-12T15:00:00"),
            sluttTidspunkt = LocalDateTime.parse("2020-07-12T15:00:00"),
            stasjonId = 1,
            partnerId = 1,
            gjentakelsesRegel = gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                startTidspunkt = recurringForm.startTidspunkt.plusDays(counter + 1),
                sluttTidspunkt = recurringForm.sluttTidspunkt.plusDays(counter + 1)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(5, counter)
    }

    @Test
    fun testHighCount() {
        val gjentakelsesRegel = GjentakelsesRegel(
            antall = 7,
            dager = everyday()
        )

        val recurringForm = UttakPostForm(
            startTidspunkt = LocalDateTime.parse("2020-07-13T15:00:00"),
            sluttTidspunkt = LocalDateTime.parse("2020-07-13T15:00:00"),
            partnerId = 1,
            stasjonId = 1,
            gjentakelsesRegel = gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                startTidspunkt = recurringForm.startTidspunkt.plusDays(counter),
                sluttTidspunkt = recurringForm.sluttTidspunkt.plusDays(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(49, counter)
    }

    @Test
    fun testFormWithGjentakelsesRegelWithoutDays() {
        val gjentakelsesRegel = GjentakelsesRegel(
            antall= 8,
            dager = listOf()
        )

        val recurringForm = UttakPostForm(
            startTidspunkt = LocalDateTime.parse("2020-07-13T15:00:00"),
            sluttTidspunkt = LocalDateTime.parse("2020-07-13T15:00:00"),
            partnerId = 1,
            stasjonId = 1,
            gjentakelsesRegel = gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                startTidspunkt = recurringForm.startTidspunkt.plusWeeks(counter),
                sluttTidspunkt = recurringForm.sluttTidspunkt.plusWeeks(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
        assertEquals(8, counter)
    }

    @Test
    fun testCountWorkdaysBetween() {
        val startTidspunkt = LocalDateTime.parse("2020-11-02T10:00:00")
        val sluttTidspunkt = LocalDateTime.parse("2020-11-12T15:00:00")

        val numberOfWorkdays = (startTidspunkt..sluttTidspunkt).getNumberOfWorkdaysBetween()
        assertEquals(numberOfWorkdays, 9)
    }

    @Test
    fun testFormEveryDayWithUntil() {
        val startTidspunkt = LocalDateTime.parse("2020-07-13T10:00:00")
        val sluttTidspunkt = LocalDateTime.parse("2020-07-29T15:00:00")
        val gjentakelsesRegel = GjentakelsesRegel(
            until= sluttTidspunkt,
            antall = 10, // 3
            dager = everyday()
        )
        val recurringForm = UttakPostForm(
            startTidspunkt = startTidspunkt,
            sluttTidspunkt = startTidspunkt.plusHours(5),
            stasjonId = 1,
            partnerId = 1,
            gjentakelsesRegel = gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                startTidspunkt = recurringForm.startTidspunkt.plusDays(counter),
                sluttTidspunkt = recurringForm.sluttTidspunkt.plusDays(counter)
            )

            assertEquals(expectedForm, actualForm)
            counter++
        }
    }


    @Test
    fun testFormWeekdayWithUntil() {
        val gjentakelsesRegel = GjentakelsesRegel(
            until= LocalDateTime.parse("2020-07-17T15:00:00"),
            dager = everyWeekDay()
        )

        val recurringForm = UttakPostForm(
            startTidspunkt = LocalDateTime.parse("2020-07-13T15:00:00"),
            sluttTidspunkt = LocalDateTime.parse("2020-07-13T15:00:00"),
            stasjonId = 1,
            partnerId = 1,
            gjentakelsesRegel = gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        var offset = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                startTidspunkt = recurringForm.startTidspunkt.plusDays(counter + offset),
                sluttTidspunkt = recurringForm.sluttTidspunkt.plusDays(counter + offset)
            )

            assertEquals(expectedForm, actualForm)
            counter++
            if (counter == 5L) offset += 2
        }
        assertEquals(5, counter)
    }

    @Test
    fun testFormWeekdaySkipWednesdayWithUntil() {
        val gjentakelsesRegel = GjentakelsesRegel(
            until= LocalDateTime.parse("2020-07-20T15:00:00"),
            dager = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))

        val recurringForm = UttakPostForm(
            startTidspunkt = LocalDateTime.parse("2020-07-13T15:00:00"),
            sluttTidspunkt = LocalDateTime.parse("2020-07-13T15:00:00"),
            partnerId = 1,
            stasjonId = 1,
            gjentakelsesRegel = gjentakelsesRegel

        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        var offset = 0L
        for (actualForm in recurringForm) {
            val expectedForm = recurringForm.copy(
                startTidspunkt = recurringForm.startTidspunkt.plusDays(counter + offset),
                sluttTidspunkt = recurringForm.sluttTidspunkt.plusDays(counter + offset)
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
            startTidspunkt = LocalDateTime.parse("2020-07-12T15:00:00"),
            sluttTidspunkt = LocalDateTime.parse("2020-07-12T15:00:00"),
            partnerId = 1,
            stasjonId = 1
        )

        // Iterate over recurringUttak and check that it matches the expected uttak.
        var counter = 0L
        for (actualForm in nonRecurringForm) {
            val expectedForm = nonRecurringForm.copy(
                startTidspunkt = nonRecurringForm.startTidspunkt.plusDays(counter),
                sluttTidspunkt = nonRecurringForm.sluttTidspunkt.plusDays(counter)
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
