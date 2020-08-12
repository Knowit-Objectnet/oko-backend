package shared.validation

import ombruk.backend.shared.utils.validation.isValid
import org.junit.jupiter.api.Test
import org.valiktor.ConstraintViolationException
import org.valiktor.validate
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertFailsWith

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
    Time list size cannot be greater than two
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
        assertFailsWith(ConstraintViolationException::class){
            validate(hours){
                validate(OpenHours::hours).isValid()
            }
        }
    }
}