package no.oslokommune.ombruk.partner.forms

import arrow.core.Either
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.partner.form.PartnerPostForm
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
        PartnerPostForm("test1", "beskrivelse1", "81549301", "test1@test.com"),
        PartnerPostForm("test2", "beskrivelse2", "81549302", "test2@test.com"),
        PartnerPostForm("test3", "beskrivelse3", "81549303", "test3@test.com"),
        PartnerPostForm("test4", "beskrivelse4", "81549304", "test4@test.com"),
        PartnerPostForm("test5", "beskrivelse5", "81549305", "test5@test.com"),
        PartnerPostForm("test6", "beskrivelse6", "+4781549305", "test6@test.com")
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
        PartnerPostForm("notUnique", "desc", "12345678", "test@test.com"),
        PartnerPostForm("badPhoneNo", "desc", "123", "test@test.com"),
        PartnerPostForm("badEmail", "desc", "12345678", "test.test.com"),
        PartnerPostForm("", "desc", "+4712345678", "test@test.com"),
        PartnerPostForm("emptyDesc", "", "+4712345678", "test@test.com"),
        PartnerPostForm("emptyPhone", "desc", "", "test@test.com"),
        PartnerPostForm("emptyMail", "desc", "+47", "")

    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: PartnerPostForm) {
        every { PartnerRepository.exists("notUnique") } returns true
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }
}