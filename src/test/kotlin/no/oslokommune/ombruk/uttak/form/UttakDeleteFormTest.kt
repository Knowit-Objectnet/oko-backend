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
        UttakDeleteForm(),
        UttakDeleteForm(1),
        UttakDeleteForm(recurrenceRuleId = 1),
        UttakDeleteForm(stasjonId = 1),
        UttakDeleteForm(partnerId = 1),
        UttakDeleteForm(fromDate = LocalDateTime.now()),
        UttakDeleteForm(toDate = LocalDateTime.now()),
        UttakDeleteForm(fromDate = LocalDateTime.now(), toDate = LocalDateTime.now().plusDays(1)),
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
        UttakDeleteForm(recurrenceRuleId = 0),
        UttakDeleteForm(stasjonId = 0),
        UttakDeleteForm(partnerId = 0),
        UttakDeleteForm(0, fromDate = LocalDateTime.now()),
        UttakDeleteForm(0, toDate = LocalDateTime.now()),
        UttakDeleteForm(fromDate = LocalDateTime.now(), toDate = LocalDateTime.now().minusDays(1)),
        UttakDeleteForm(1, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0, 1)

    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: UttakDeleteForm) {
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}