package no.oslokommune.ombruk.uttaksforesporsel.form

import arrow.core.Either
import no.oslokommune.ombruk.uttaksforesporsel.form.pickup.PickupDeleteForm
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PickupDeleteFormTest {

    @Test
    fun `validate valid form`() {
        val form = PickupDeleteForm(1)
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Test
    fun `validate invalid form`() {
        val form = PickupDeleteForm(0)
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}