package calendar.form.event

import arrow.core.Either
import ombruk.backend.calendar.form.event.EventGetByIdForm
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventGetByIdFormTest {

    @Test
    fun `validate valid form`() {
        val form = EventGetByIdForm(1)
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Test
    fun `validate invalid form`() {
        val form = EventGetByIdForm(0)
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}