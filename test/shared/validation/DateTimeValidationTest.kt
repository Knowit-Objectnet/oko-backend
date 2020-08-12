package shared.validation

import ombruk.backend.shared.utils.validation.isGreaterThanStartDateTime
import ombruk.backend.shared.utils.validation.isLessThanEndDateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.valiktor.ConstraintViolationException
import org.valiktor.validate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DateTimeValidationTest {

    data class DateTimeTest(val localDateTime: LocalDateTime)

    /*
    Start date Time is lesser than End Date Time, does not throw exception
     */
    @Test
    fun `end date is greater than start date`() {
        val dateTimeTest = DateTimeTest(LocalDateTime.parse("2020-07-06T15:48:06Z", DateTimeFormatter.ISO_DATE_TIME))
        validate(dateTimeTest) {
            validate(DateTimeTest::localDateTime).isLessThanEndDateTime(
                LocalDateTime.parse(
                    "2020-07-06T15:49:06Z",
                    DateTimeFormatter.ISO_DATE_TIME
                )
            )
        }
    }

    /*
    start date time cannot be greater than end date time, throws exception
     */
    @Test
    fun `start date is greater than end date`() {
        val dateTimeTest = DateTimeTest(LocalDateTime.parse("2020-07-06T15:48:06Z", DateTimeFormatter.ISO_DATE_TIME))
        assertFailsWith(exceptionClass = ConstraintViolationException::class) {
            validate(dateTimeTest) {
                validate(DateTimeTest::localDateTime).isLessThanEndDateTime(
                    LocalDateTime.parse("2020-07-05T15:45:06Z", DateTimeFormatter.ISO_DATE_TIME)
                )
            }
        }
    }

    /*
    Start date is lesser than end date, does not throw exception
     */
    @Test
    fun `start date is lesser than end date`() {
        val dateTimeTest = DateTimeTest(LocalDateTime.parse("2020-07-08T15:48:06Z", DateTimeFormatter.ISO_DATE_TIME))
        validate(dateTimeTest) {
            validate(DateTimeTest::localDateTime).isGreaterThanStartDateTime(
                LocalDateTime.parse(
                    "2020-07-06T15:49:06Z",
                    DateTimeFormatter.ISO_DATE_TIME
                )
            )
        }
    }

    /*
    end date time cannot be lesser than start date time, throws exception
     */
    @Test
    fun `end date time is lesser than start date`() {
        val dateTimeTest = DateTimeTest(LocalDateTime.parse("2020-07-04T15:48:06Z", DateTimeFormatter.ISO_DATE_TIME))
        assertFailsWith(exceptionClass = ConstraintViolationException::class){
            validate(dateTimeTest) {
                validate(DateTimeTest::localDateTime).isGreaterThanStartDateTime(
                    LocalDateTime.parse("2020-07-05T15:45:06Z", DateTimeFormatter.ISO_DATE_TIME)
                )
            }
        }
    }


}