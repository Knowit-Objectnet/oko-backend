package no.oslokommune.ombruk.reporting.form

import arrow.core.Either
import no.oslokommune.ombruk.reporting.form.ReportUpdateForm
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReportUpdateFormTest {

    @Suppress("unused")
    fun generateValidForms() = listOf(
        ReportUpdateForm(1, 123)
    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: ReportUpdateForm) {

        val result = form.validOrError()
        println(result)
        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        ReportUpdateForm(1, 0),
        ReportUpdateForm(0, 1)

    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: ReportUpdateForm) {

        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}