package no.oslokommune.ombruk.partner.forms

import arrow.core.Either
import no.oslokommune.ombruk.partner.form.PartnerGetByIdForm

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PartnerGetByIdFormTest {

    @Test
    fun `validate valid form`() {
        val form = PartnerGetByIdForm(1)
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Test
    fun `validate invalid form`() {
        val form = PartnerGetByIdForm(0)
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}