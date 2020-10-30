package no.oslokommune.ombruk.partner.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import no.oslokommune.ombruk.partner.database.SamPartnerRepository
import no.oslokommune.ombruk.partner.form.PartnerDeleteForm
import no.oslokommune.ombruk.partner.form.PartnerGetForm
import no.oslokommune.ombruk.partner.form.PartnerPostForm
import no.oslokommune.ombruk.partner.form.PartnerUpdateForm
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.shared.api.KeycloakGroupIntegration
import no.oslokommune.ombruk.shared.database.initDB
import no.oslokommune.ombruk.shared.error.KeycloakIntegrationError
import no.oslokommune.ombruk.shared.error.RepositoryError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class PartnerServiceTest {


    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        mockkObject(SamPartnerRepository)
        mockkObject(KeycloakGroupIntegration)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @AfterAll
    fun finish() {
        unmockkAll()
    }

    @Nested
    inner class SavePartner {

        @Test
        fun `save partner success`(@MockK form: PartnerPostForm, @MockK(relaxed = true) expected: Partner) {
            every { SamPartnerRepository.insertPartner(form) } returns expected.right()
            every { KeycloakGroupIntegration.createGroup(expected.navn, expected.id) } returns 1.right()

            val actual = PartnerService.savePartner(form)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `save partner repository error`(
            @MockK form: PartnerPostForm,
            @MockK expected: RepositoryError.InsertError
        ) {
            every { SamPartnerRepository.insertPartner(form) } returns expected.left()

            val actual = PartnerService.savePartner(form)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        @Test
        fun `save partner keycloak error`(
            @MockK(relaxed = true) partner: Partner,
            @MockK form: PartnerPostForm,
            @MockK expected: KeycloakIntegrationError
        ) {
            every { SamPartnerRepository.insertPartner(form) } returns partner.right()
            every { KeycloakGroupIntegration.createGroup(partner.navn, partner.id) } returns expected.left()

            val actual = PartnerService.savePartner(form)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

    @Nested
    inner class GetPartnerById {

        @Test
        fun `get partner by id success`(@MockK expected: Partner) {
            every { SamPartnerRepository.getPartnerByID(1) } returns expected.right()

            val actual = PartnerService.getPartnerById(1)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `get partner by id repository error`(@MockK expected: RepositoryError.NoRowsFound) {
            every { SamPartnerRepository.getPartnerByID(1) } returns expected.left()

            val actual = PartnerService.getPartnerById(1)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

    @Nested
    inner class GetSamarbeidspartnere {

        @Test
        fun `get partnere success`(@MockK expected: List<Partner>) {
            every { SamPartnerRepository.getPartnere(PartnerGetForm()) } returns expected.right()

            val actual = PartnerService.getPartnere()

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `get partner repository error`(@MockK expected: RepositoryError.SelectError) {
            every { SamPartnerRepository.getPartnere(PartnerGetForm()) } returns expected.left()

            val actual = PartnerService.getPartnere()

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

    @Nested
    inner class DeletePartner {

        @Test
        fun `delete partner success`(@MockK(relaxed = true) expected: Partner) {

            every { SamPartnerRepository.getPartnerByID(expected.id) } returns expected.right()
            every { SamPartnerRepository.deletePartner(expected.id) } returns Unit.right()
            every { KeycloakGroupIntegration.deleteGroup(expected.navn) } returns Unit.right()

            val actual = PartnerService.deletePartnerById(expected.id)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `delete partner repository error`(
            @MockK partner: Partner,
            @MockK(relaxed = true) form: PartnerDeleteForm,
            @MockK expected: RepositoryError.DeleteError
        ) {

            every { SamPartnerRepository.getPartnerByID(form.id) } returns partner.right()
            every { SamPartnerRepository.deletePartner(form.id) } returns expected.left()

            val actual = PartnerService.deletePartnerById(form.id)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        @Test
        fun `delete partner keycloak error`(
            @MockK(relaxed = true) partner: Partner,
            @MockK(relaxed = true) form: PartnerDeleteForm,
            @MockK expected: KeycloakIntegrationError
        ) {
            every { SamPartnerRepository.getPartnerByID(form.id) } returns partner.right()
            every { SamPartnerRepository.deletePartner(form.id) } returns Unit.right()

            every { KeycloakGroupIntegration.deleteGroup(partner.navn) } returns expected.left()

            val actual = PartnerService.deletePartnerById(form.id)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

    @Nested
    inner class UpdatePartner {

        @Test
        fun `update partner success`(
            @MockK(relaxed = true) initial: Partner,
            @MockK(relaxed = true) expected: Partner
        ) {
            val form = PartnerUpdateForm(initial.id, expected.navn, expected.beskrivelse, expected.telefon, expected.epost)

            every { SamPartnerRepository.getPartnerByID(expected.id) } returns initial.right()
            every { SamPartnerRepository.updatePartner(form) } returns expected.right()
            every { KeycloakGroupIntegration.updateGroup(initial.navn, expected.navn) } returns Unit.right()

            val actual = PartnerService.updatePartner(form)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `update partner repository error`(
            @MockK partner: Partner,
            @MockK(relaxed = true) form: PartnerUpdateForm,
            @MockK expected: RepositoryError.UpdateError
        ) {

            every { SamPartnerRepository.getPartnerByID(form.id) } returns partner.right()
            every { SamPartnerRepository.updatePartner(form) } returns expected.left()

            val actual = PartnerService.updatePartner(form)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        @Test
        fun `update partner keycloak error`(
            @MockK(relaxed = true) partner: Partner,
            @MockK(relaxed = true) form: PartnerUpdateForm,
            @MockK expected: KeycloakIntegrationError
        ) {

            every { SamPartnerRepository.getPartnerByID(form.id) } returns partner.right()
            every { SamPartnerRepository.updatePartner(form) } returns partner.right()
            every { KeycloakGroupIntegration.updateGroup(partner.navn, form.navn) } returns expected.left()

            val actual = PartnerService.updatePartner(form)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }
}