package no.oslokommune.ombruk.shared.validation

import no.oslokommune.ombruk.shared.utils.validation.isGreaterThanOpeningTime
import no.oslokommune.ombruk.shared.utils.validation.isLessThanClosingTime
import org.junit.jupiter.api.Test
import org.valiktor.ConstraintViolationException
import org.valiktor.validate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertFailsWith

class TimeValidationTest {

    data class TimeTest(val localTime: LocalTime)

    /*
    Start Time is lesser than End Time, does not throw exception
     */
    @Test
    fun `end time is greater than start time`() {
        val timeTest = TimeTest(LocalTime.parse("08:00:00Z", DateTimeFormatter.ISO_TIME))
        validate(timeTest) {
            validate(TimeTest::localTime).isLessThanClosingTime(
                LocalTime.parse(
                    "20:00:00Z",
                    DateTimeFormatter.ISO_TIME
                )
            )
        }
    }

    /*
    start time cannot be greater than end time, throws exception
     */
    @Test
    fun `start time is greater than end time`() {
        val timeTest = TimeTest(LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME))
        assertFailsWith(exceptionClass = ConstraintViolationException::class) {
            validate(timeTest) {
                validate(TimeTest::localTime).isLessThanClosingTime(
                    LocalTime.parse("19:00:00Z", DateTimeFormatter.ISO_TIME)
                )
            }
        }
    }

    /*
    Start time is lesser than end time, does not throw exception
     */
    @Test
    fun `start time is lesser than end time`() {
        val timeTest = TimeTest(LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME))
        validate(timeTest) {
            validate(TimeTest::localTime).isGreaterThanOpeningTime(
                LocalTime.parse(
                    "08:00:00Z",
                    DateTimeFormatter.ISO_TIME
                )
            )
        }
    }

    /*
    end time cannot be lesser than start time, throws exception
     */
    @Test
    fun `end time time is lesser than start time`() {
        val timeTest = TimeTest(LocalTime.parse("08:00:00Z", DateTimeFormatter.ISO_TIME))
        assertFailsWith(exceptionClass = ConstraintViolationException::class) {
            validate(timeTest) {
                validate(TimeTest::localTime).isGreaterThanOpeningTime(
                    LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                )
            }
        }
    }
}