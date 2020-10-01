package no.oslokommune.ombruk.station.form

import arrow.core.Either
import no.oslokommune.ombruk.station.form.StationGetForm
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StationGetFormTest {

    @Suppress("unused")
    fun generateValidForms() = listOf(StationGetForm(), StationGetForm("notBlank"))

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: StationGetForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Test
    fun `validate invalid form`() {
        val form = StationGetForm("")
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}