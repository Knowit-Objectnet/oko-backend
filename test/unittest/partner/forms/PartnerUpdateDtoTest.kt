package partner.forms

import arrow.core.Either
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.partner.form.PartnerUpdateForm
import ombruk.backend.partner.model.Partner
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
class PartnerUpdateDtoTest {

    val existingPartner = Partner(1, "unique")

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
        PartnerUpdateForm(1, "unique"),
        PartnerUpdateForm(1, "unique", "desc"),
        PartnerUpdateForm(1, "unique", phone = "12345678"),
        PartnerUpdateForm(1, "unique", email = "test@gmail.com"),
        PartnerUpdateForm(1, "unique", "desc", "12345678", "test@gmail.com")
    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: PartnerUpdateForm) {
        val result = form.validOrError()

        every { PartnerRepository.getPartnerByID(existingPartner.id) } returns existingPartner.right()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        PartnerUpdateForm(1, ""),
        PartnerUpdateForm(1, "notUnique", "desc"),
        PartnerUpdateForm(1, "unique", ""),
        PartnerUpdateForm(1, "unique", phone = "123456789"),
        PartnerUpdateForm(1, "unique", email = "test£gmail.com"),
        PartnerUpdateForm(1, "unique", "", "12345678", "test@gmail.com")
    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: PartnerUpdateForm) {

        every { PartnerRepository.getPartnerByID(existingPartner.id) } returns existingPartner.right()
        every { PartnerRepository.exists("notUnique") } returns true

        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}