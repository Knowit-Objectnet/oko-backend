package no.oslokommune.ombruk.uttak.form

import arrow.core.Either
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UttakDeleteFormTest {

    @Suppress("unused")
    fun generateValidForms() = listOf(
        UttakDeleteForm(1),
        UttakDeleteForm(gjentakelsesRegelId = 1),
        UttakDeleteForm(stasjonId = 1),
        UttakDeleteForm(partnerId = 1),
        UttakDeleteForm(startTidspunkt = LocalDateTime.now()),
        UttakDeleteForm(sluttTidspunkt = LocalDateTime.now()),
        UttakDeleteForm(startTidspunkt = LocalDateTime.now(), sluttTidspunkt = LocalDateTime.now().plusDays(1)),
        UttakDeleteForm(1, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, 1)

    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: UttakDeleteForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        UttakDeleteForm(0),
        UttakDeleteForm(gjentakelsesRegelId = 0),
        UttakDeleteForm(stasjonId = 0),
        UttakDeleteForm(partnerId = 0),
        UttakDeleteForm(0, startTidspunkt = LocalDateTime.now()),
        UttakDeleteForm(0, sluttTidspunkt = LocalDateTime.now()),
        UttakDeleteForm(startTidspunkt = LocalDateTime.now(), sluttTidspunkt = LocalDateTime.now().minusDays(1)),
        UttakDeleteForm(1, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0, 1)
    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: UttakDeleteForm) {
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}