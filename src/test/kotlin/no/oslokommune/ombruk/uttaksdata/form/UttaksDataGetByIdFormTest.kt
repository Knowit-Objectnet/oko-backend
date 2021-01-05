package no.oslokommune.ombruk.uttaksdata.form

import arrow.core.Either

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UttaksDataGetByIdFormTest {

    @Test
    fun `validate valid form`() {
        val form = UttaksDataGetByIdForm(1)
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Test
    fun `validate invalid form`() {
        val form = UttaksDataGetByIdForm(0)
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}