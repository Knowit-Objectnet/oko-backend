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
        UttakGetForm(gjentakelsesRegelID = 1),
        UttakGetForm(stasjonID = 1),
        UttakGetForm(partnerID = 1),
        UttakGetForm(startTidspunkt = LocalDateTime.now()),
        UttakGetForm(sluttTidspunkt = LocalDateTime.now()),
        UttakGetForm(startTidspunkt = LocalDateTime.now(), sluttTidspunkt = LocalDateTime.now().plusDays(1)),
        UttakGetForm(
            stasjonID = 1,
            partnerID = 1,
            gjentakelsesRegelID = 1,
            startTidspunkt = LocalDateTime.now(),
            sluttTidspunkt = LocalDateTime.now().plusDays(1)
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
        UttakGetForm(gjentakelsesRegelID = 0),
        UttakGetForm(stasjonID = 0),
        UttakGetForm(partnerID = 0),
        UttakGetForm(0, startTidspunkt = LocalDateTime.now()),
        UttakGetForm(0, sluttTidspunkt = LocalDateTime.now()),
        UttakGetForm(startTidspunkt = LocalDateTime.now(), sluttTidspunkt = LocalDateTime.now().minusDays(1)),
        UttakGetForm(1, 1, 1, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1))

    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: UttakGetForm) {
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}