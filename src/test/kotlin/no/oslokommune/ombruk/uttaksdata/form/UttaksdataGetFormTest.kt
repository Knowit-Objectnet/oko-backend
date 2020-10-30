package no.oslokommune.ombruk.uttaksdata.form

import arrow.core.Either
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UttaksdataGetFormTest {

    @Suppress("unused")
    fun generateValidForms() = listOf(
        UttaksdataGetForm(),
        UttaksdataGetForm(1),
        UttaksdataGetForm(stasjonId = 1),
        UttaksdataGetForm(partnerId = 1),
        UttaksdataGetForm(fromDate = LocalDateTime.now()),
        UttaksdataGetForm(fraRapportertTidspunkt = LocalDateTime.now()),
        UttaksdataGetForm(fromDate = LocalDateTime.now(), fraRapportertTidspunkt = LocalDateTime.now().plusHours(1))
    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: UttaksdataGetForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        UttaksdataGetForm(0),
        UttaksdataGetForm(stasjonId = 0),
        UttaksdataGetForm(partnerId = 0),
        UttaksdataGetForm(fromDate = LocalDateTime.now(), fraRapportertTidspunkt = LocalDateTime.now().minusHours(1)),
        UttaksdataGetForm(1, 1, 1, LocalDateTime.now(), LocalDateTime.now().plusHours(1))
    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: UttaksdataGetForm) {
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}