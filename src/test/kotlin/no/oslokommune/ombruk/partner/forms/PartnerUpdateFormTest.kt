package no.oslokommune.ombruk.partner.forms

import arrow.core.Either
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import no.oslokommune.ombruk.partner.database.SamPartnerRepository
import no.oslokommune.ombruk.partner.form.PartnerUpdateForm
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.shared.database.initDB
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
class PartnerUpdateFormTest {

    val existingPartner = Partner(1, "unique", "beskrivelse", "81549300", "test@test.com")

    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        mockkObject(SamPartnerRepository)
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
        PartnerUpdateForm(1, "test1", "beskrivelse1", "81549301", "test1@test.com")
    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: PartnerUpdateForm) {
        val result = form.validOrError()

        every { SamPartnerRepository.getPartnerByID(existingPartner.id) } returns existingPartner.right()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        PartnerUpdateForm(1, "notUnique", "desc", "12345678", "test@test.com"),
        PartnerUpdateForm(1, "unique2", "desc", "12345678", "test@test.com"),
        PartnerUpdateForm(1, "badPhoneNo", "desc", "123", "test@test.com"),
        PartnerUpdateForm(1, "badEmail", "desc", "12345678", "memes")
    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: PartnerUpdateForm) {

        every { SamPartnerRepository.getPartnerByID(existingPartner.id) } returns existingPartner.right()
        form.navn?.let {
            every { SamPartnerRepository.exists(it) } returns true
            every { SamPartnerRepository.exists(existingPartner.navn) } returns true
        }

        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}