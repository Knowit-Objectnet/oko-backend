package no.oslokommune.ombruk.uttak.form

import arrow.core.Either
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UttakGetFormTest {

    @Suppress("unused")
    fun generateValidForms() = listOf(
        UttakGetForm(),
        UttakGetForm(1),
        UttakGetForm(recurrenceRuleId = 1),
        UttakGetForm(stationId = 1),
        UttakGetForm(partnerId = 1),
        UttakGetForm(fromDate = LocalDateTime.now()),
        UttakGetForm(toDate = LocalDateTime.now()),
        UttakGetForm(fromDate = LocalDateTime.now(), toDate = LocalDateTime.now().plusDays(1)),
        UttakGetForm(
            stationId = 1,
            partnerId = 1,
            recurrenceRuleId = 1,
            fromDate = LocalDateTime.now(),
            toDate = LocalDateTime.now().plusDays(1)
        )

    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: UttakGetForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        UttakGetForm(0),
        UttakGetForm(recurrenceRuleId = 0),
        UttakGetForm(stationId = 0),
        UttakGetForm(partnerId = 0),
        UttakGetForm(0, fromDate = LocalDateTime.now()),
        UttakGetForm(0, toDate = LocalDateTime.now()),
        UttakGetForm(fromDate = LocalDateTime.now(), toDate = LocalDateTime.now().minusDays(1)),
        UttakGetForm(1, 1, 1, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1))

    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: UttakGetForm) {
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}