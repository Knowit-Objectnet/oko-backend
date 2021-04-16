package partner.forms

import arrow.core.Either
import ombruk.backend.partner.form.PartnerGetForm
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PartnerGetDtoTest {

    @Suppress("unused")
    fun generateValidForms() = listOf(PartnerGetForm(), PartnerGetForm("notBlank"))

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: PartnerGetForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Test
    fun `validate invalid form`() {
        val form = PartnerGetForm("")
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}