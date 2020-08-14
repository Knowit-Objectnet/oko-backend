package calendar.form.event

import arrow.core.Either
import ombruk.backend.calendar.form.event.EventGetForm
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventGetFormTest {

    @Suppress("unused")
    fun generateValidForms() = listOf(
        EventGetForm(),
        EventGetForm(1),
        EventGetForm(recurrenceRuleId = 1),
        EventGetForm(stationId = 1),
        EventGetForm(partnerId = 1),
        EventGetForm(fromDate = LocalDateTime.now()),
        EventGetForm(toDate = LocalDateTime.now()),
        EventGetForm(fromDate = LocalDateTime.now(), toDate = LocalDateTime.now().plusDays(1)),
        EventGetForm(
            stationId = 1,
            partnerId = 1,
            recurrenceRuleId = 1,
            fromDate = LocalDateTime.now(),
            toDate = LocalDateTime.now().plusDays(1)
        )

    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: EventGetForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        EventGetForm(0),
        EventGetForm(recurrenceRuleId = 0),
        EventGetForm(stationId = 0),
        EventGetForm(partnerId = 0),
        EventGetForm(0, fromDate = LocalDateTime.now()),
        EventGetForm(0, toDate = LocalDateTime.now()),
        EventGetForm(fromDate = LocalDateTime.now(), toDate = LocalDateTime.now().minusDays(1)),
        EventGetForm(1, 1, 1, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1))

    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: EventGetForm) {
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}