package calendar.form.event

import arrow.core.Either
import ombruk.backend.calendar.form.event.EventDeleteForm
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventDeleteFormTest {

    @Suppress("unused")
    fun generateValidForms() = listOf(
        EventDeleteForm(),
        EventDeleteForm(1),
        EventDeleteForm(recurrenceRuleId = 1),
        EventDeleteForm(stationId = 1),
        EventDeleteForm(partnerId = 1),
        EventDeleteForm(fromDate = LocalDateTime.now()),
        EventDeleteForm(toDate = LocalDateTime.now()),
        EventDeleteForm(fromDate = LocalDateTime.now(), toDate = LocalDateTime.now().plusDays(1)),
        EventDeleteForm(1, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, 1)

    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: EventDeleteForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        EventDeleteForm(0),
        EventDeleteForm(recurrenceRuleId = 0),
        EventDeleteForm(stationId = 0),
        EventDeleteForm(partnerId = 0),
        EventDeleteForm(0, fromDate = LocalDateTime.now()),
        EventDeleteForm(0, toDate = LocalDateTime.now()),
        EventDeleteForm(fromDate = LocalDateTime.now(), toDate = LocalDateTime.now().minusDays(1)),
        EventDeleteForm(1, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0, 1)

    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: EventDeleteForm) {
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}