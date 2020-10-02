package no.oslokommune.ombruk.uttaksdata.form

import arrow.core.Either
import no.oslokommune.ombruk.uttaksdata.form.ReportGetByIdForm

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UttaksdataGetByIdFormTest {

    @Test
    fun `validate valid form`() {
        val form = ReportGetByIdForm(1)
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Test
    fun `validate invalid form`() {
        val form = ReportGetByIdForm(0)
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}