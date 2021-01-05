package no.oslokommune.ombruk.stasjon.form

import arrow.core.Either
import no.oslokommune.ombruk.stasjon.form.StasjonGetForm
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StasjonGetFormTest {

    @Suppress("unused")
    fun generateValidForms() = listOf(StasjonGetForm(), StasjonGetForm("notBlank"))

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: StasjonGetForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Test
    fun `validate invalid form`() {
        val form = StasjonGetForm("")
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}