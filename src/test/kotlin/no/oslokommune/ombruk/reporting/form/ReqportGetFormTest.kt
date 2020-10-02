package no.oslokommune.ombruk.uttaksdata.form

import arrow.core.Either
import no.oslokommune.ombruk.uttaksdata.form.ReportGetForm
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReqportGetFormTest {

    @Suppress("unused")
    fun generateValidForms() = listOf(
        ReportGetForm(),
        ReportGetForm(1),
        ReportGetForm(stasjonId = 1),
        ReportGetForm(partnerId = 1),
        ReportGetForm(fromDate = LocalDateTime.now()),
        ReportGetForm(toDate = LocalDateTime.now()),
        ReportGetForm(fromDate = LocalDateTime.now(), toDate = LocalDateTime.now().plusHours(1))
    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: ReportGetForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        ReportGetForm(0),
        ReportGetForm(stasjonId = 0),
        ReportGetForm(partnerId = 0),
        ReportGetForm(fromDate = LocalDateTime.now(), toDate = LocalDateTime.now().minusHours(1)),
        ReportGetForm(1, 1, 1, LocalDateTime.now(), LocalDateTime.now().plusHours(1))
    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: ReportGetForm) {
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}