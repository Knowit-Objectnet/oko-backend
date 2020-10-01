package no.oslokommune.ombruk.shared.validation

import no.oslokommune.ombruk.shared.utils.validation.isValid
import no.oslokommune.ombruk.shared.utils.validation.isWithin
import org.junit.jupiter.api.Test
import org.valiktor.ConstraintViolationException
import org.valiktor.validate
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OpenHoursValidationTest {

    data class OpenHours(val hours: Map<DayOfWeek, List<LocalTime>>?)

    /*
    If hours is null, station is always closed and thus valid
     */
    @Test
    fun `null hours valid`() {
        val hours = OpenHours(null)
        validate(hours) {
            validate(OpenHours::hours).isValid()
        }
    }

    /*
    Days within MONDAY-FRIDAY should be valid as long as their corresponding array lengths = 2
     */
    @Test
    fun `hours valid`() {
        val hours = OpenHours(
            mapOf<DayOfWeek, List<LocalTime>>(
                Pair(
                    DayOfWeek.MONDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.TUESDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.WEDNESDAY,
                    listOf(
                        LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("21:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.FRIDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                )
            )
        )
        validate(hours) {
            validate(OpenHours::hours).isValid()
        }
    }

    /*
    hours outside of MONDAY-FRIDAY is not valid
     */
    @Test
    fun `hours invalid DayOfWeek value`() {
        val hours = OpenHours(
            mapOf<DayOfWeek, List<LocalTime>>(
                Pair(
                    DayOfWeek.MONDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.TUESDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.WEDNESDAY,
                    listOf(
                        LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("21:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.FRIDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.SATURDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.SUNDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                )
            )
        )
        assertFailsWith(ConstraintViolationException::class) {
            validate(hours) {
                validate(OpenHours::hours).isValid()
            }
        }
    }

    /*
    Time list size cannot be greater than two
     */
    @Test
    fun `hours invalid list size greater`() {
        val hours = OpenHours(
            mapOf<DayOfWeek, List<LocalTime>>(
                Pair(
                    DayOfWeek.MONDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.TUESDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.WEDNESDAY,
                    listOf(
                        LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("21:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.FRIDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.SATURDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.SUNDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                )
            )
        )
        assertFailsWith(ConstraintViolationException::class) {
            validate(hours) {
                validate(OpenHours::hours).isValid()
            }
        }
    }

    /*
    Time list size cannot be lesser than two
     */
    @Test
    fun `hours invalid list size lesser`() {
        val hours = OpenHours(
            mapOf<DayOfWeek, List<LocalTime>>(
                Pair(
                    DayOfWeek.MONDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.TUESDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.WEDNESDAY,
                    listOf(
                        LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("21:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.FRIDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.SATURDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.SUNDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                )
            )
        )
        assertFailsWith(ConstraintViolationException::class) {
            validate(hours) {
                validate(OpenHours::hours).isValid()
            }
        }
    }

    /*
    Time list size cannot be empty
     */
    @Test
    fun `hours invalid empty list`() {
        val hours = OpenHours(
            mapOf<DayOfWeek, List<LocalTime>>(
                Pair(
                    DayOfWeek.MONDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.TUESDAY,
                    emptyList()
                ),
                Pair(
                    DayOfWeek.WEDNESDAY,
                    listOf(
                        LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("21:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.FRIDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.SATURDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.SUNDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                )
            )
        )
        assertFailsWith(ConstraintViolationException::class) {
            validate(hours) {
                validate(OpenHours::hours).isValid()
            }
        }
    }

    /*
    The first value of the list cannot be greater than the second
     */
    @Test
    fun `hours first value lesser than second`() {
        val hours = OpenHours(
            mapOf<DayOfWeek, List<LocalTime>>(
                Pair(
                    DayOfWeek.MONDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.TUESDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.WEDNESDAY,
                    listOf(
                        LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("21:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.FRIDAY,
                    listOf(
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                )
            )
        )
        assertFailsWith(ConstraintViolationException::class) {
            validate(hours) {
                validate(OpenHours::hours).isValid()
            }
        }
    }

    /*
    The first value of the list cannot be equal to the second
     */
    @Test
    fun `hours first value equal to second`() {
        val hours = OpenHours(
            mapOf<DayOfWeek, List<LocalTime>>(
                Pair(
                    DayOfWeek.MONDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.TUESDAY,
                    listOf(
                        LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.WEDNESDAY,
                    listOf(
                        LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("21:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.FRIDAY,
                    listOf(
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                    )
                )
            )
        )
        assertFailsWith(ConstraintViolationException::class) {
            validate(hours) {
                validate(OpenHours::hours).isValid()
            }
        }
    }

    @Test
    fun `Open hours is within`() {
        val hours = OpenHours(mapOf<DayOfWeek, List<LocalTime>>(
                Pair(
                        DayOfWeek.MONDAY,
                        listOf(
                                LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                                LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                        )
                ),
                Pair(
                        DayOfWeek.TUESDAY,
                        listOf(
                                LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                                LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                        )
                ),
                Pair(
                        DayOfWeek.WEDNESDAY,
                        listOf(
                                LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                                LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                        )
                ),
                Pair(
                        DayOfWeek.THURSDAY,
                        listOf(
                                LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                                LocalTime.parse("21:00:00Z", DateTimeFormatter.ISO_TIME)
                        )
                ),
                Pair(
                        DayOfWeek.FRIDAY,
                        listOf(
                                LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                                LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
                        )
                )
        ))

        val tuesdayAt10 = LocalDateTime.of(2020, 8, 25, 10, 0, 0)
        assertEquals(DayOfWeek.TUESDAY, tuesdayAt10.dayOfWeek)
        assertTrue { tuesdayAt10.isWithin(hours.hours!!) }

        val sundayAt10 = LocalDateTime.of(2020, 8, 30, 10, 0, 0)
        assertEquals(DayOfWeek.SUNDAY, sundayAt10.dayOfWeek)
        assertFalse { sundayAt10.isWithin(hours.hours!!) }

        val mondayAt8 = LocalDateTime.of(2020, 8, 24, 8, 0, 0)
        assertEquals(DayOfWeek.MONDAY, mondayAt8.dayOfWeek)
        assertFalse { mondayAt8.isWithin(hours.hours!!) }
    }
}