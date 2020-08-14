package partner.forms

import arrow.core.Either
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.partner.form.PartnerPostForm
import ombruk.backend.shared.database.initDB
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class PartnerPostFormTest {

    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        mockkObject(PartnerRepository)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @AfterAll
    fun finish() {
        unmockkAll()
    }

    @Suppress("unused")
    fun generateValidForms() = listOf(
        PartnerPostForm("unique"),
        PartnerPostForm("unique", "desc"),
        PartnerPostForm("unique", phone = "12345678"),
        PartnerPostForm("unique", email = "test@gmail.com"),
        PartnerPostForm("unique", "desc", "12345678", "test@gmail.com")
    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: PartnerPostForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        PartnerPostForm(""),
        PartnerPostForm("notUnique", "desc"),
        PartnerPostForm("unique", ""),
        PartnerPostForm("unique", phone = "123456789"),
        PartnerPostForm("unique", email = "test£gmail.com"),
        PartnerPostForm("unique", "", "12345678", "test@gmail.com")
    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: PartnerPostForm) {
        every { PartnerRepository.exists("notUnique") } returns true

        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}