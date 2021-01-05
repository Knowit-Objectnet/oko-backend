package no.oslokommune.ombruk.partner.forms

import arrow.core.Either
import no.oslokommune.ombruk.partner.form.PartnerGetForm
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PartnerGetFormTest {

    @Suppress("unused")
    fun generateValidForms() =
        listOf(
            PartnerGetForm(),
            PartnerGetForm("notBlank"),
            PartnerGetForm(telefon = "12345678"),
            PartnerGetForm(telefon = "+4712345678"),
            PartnerGetForm(beskrivelse = "Vil ha denne"),
            PartnerGetForm(epost = "meg@example.com")
        )

    fun generateInvalidForms() = listOf(
        PartnerGetForm(navn = ""),
        PartnerGetForm(telefon = ""),
        PartnerGetForm(telefon = "1234567"),
        PartnerGetForm(telefon = "+471234567"),
        PartnerGetForm(telefon = "+4712345a78"),
        PartnerGetForm(beskrivelse = ""),
        PartnerGetForm(epost = ""),
        PartnerGetForm(epost = "meg.example.com")
    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: PartnerGetForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: PartnerGetForm) {
        val result = form.validOrError()
        assertTrue(result is Either.Left)
    }

}