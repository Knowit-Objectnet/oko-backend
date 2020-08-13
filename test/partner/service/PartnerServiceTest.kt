package partner.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.partner.form.PartnerDeleteForm
import ombruk.backend.partner.form.PartnerGetForm
import ombruk.backend.partner.form.PartnerPostForm
import ombruk.backend.partner.form.PartnerUpdateForm
import ombruk.backend.partner.model.Partner
import ombruk.backend.partner.service.PartnerService
import ombruk.backend.shared.api.KeycloakGroupIntegration
import ombruk.backend.shared.database.initDB
import ombruk.backend.shared.error.KeycloakIntegrationError
import ombruk.backend.shared.error.RepositoryError
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class PartnerServiceTest {


    init {
        initDB()
    }

    @BeforeEach
    fun setup(){
        mockkObject(PartnerRepository)
        mockkObject(KeycloakGroupIntegration)
    }

    @Nested
    inner class SavePartner {

        @Test
        fun `save partner success`(@MockK form: PartnerPostForm, @MockK(relaxed = true) expected: Partner){
            every { PartnerRepository.insertPartner(form) } returns expected.right()
            every { KeycloakGroupIntegration.createGroup(expected.name, expected.id)} returns 1.right()

            val actual = PartnerService.savePartner(form)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `save partner repository error`(@MockK form: PartnerPostForm, @MockK expected: RepositoryError.InsertError){
            every { PartnerRepository.insertPartner(form) } returns expected.left()

            val actual = PartnerService.savePartner(form)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        @Test
        fun `save partner keycloak error`(@MockK(relaxed = true) partner: Partner, @MockK form: PartnerPostForm, @MockK expected: KeycloakIntegrationError){
            every { PartnerRepository.insertPartner(form) } returns partner.right()
            every { KeycloakGroupIntegration.createGroup(partner.name, partner.id)} returns expected.left()

            val actual = PartnerService.savePartner(form)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

    @Nested
    inner class GetPartnerById {

        @Test
        fun `get partner by id success`(@MockK expected: Partner){
            every { PartnerRepository.getPartnerByID(1) } returns expected.right()

            val actual = PartnerService.getPartnerById(1)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `get partner by id repository error`(@MockK expected: RepositoryError.NoRowsFound){
            every { PartnerRepository.getPartnerByID(1) } returns expected.left()

            val actual = PartnerService.getPartnerById(1)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

    @Nested
    inner class GetPartners {

        @Test
        fun `get partners success`(@MockK expected: List<Partner>){
            every { PartnerRepository.getPartners(PartnerGetForm()) } returns expected.right()

            val actual = PartnerService.getPartners()

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `get partner repository error`(@MockK expected: RepositoryError.SelectError){
            every {  PartnerRepository.getPartners(PartnerGetForm()) } returns expected.left()

            val actual = PartnerService.getPartners()

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

    @Nested
    inner class DeletePartner {

        @Test
        fun `delete partner success`(@MockK(relaxed = true) expected: Partner) {

            every { PartnerRepository.getPartnerByID(expected.id) } returns expected.right()
            every { PartnerRepository.deletePartner(expected.id) } returns Unit.right()
            every {KeycloakGroupIntegration.deleteGroup(expected.name)} returns Unit.right()

            val actual = PartnerService.deletePartnerById(expected.id)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `delete partner repository error`(@MockK partner: Partner, @MockK(relaxed = true) form: PartnerDeleteForm, @MockK expected: RepositoryError.DeleteError){

            every { PartnerRepository.getPartnerByID(form.id) } returns partner.right()
            every { PartnerRepository.deletePartner(form.id) } returns expected.left()

            val actual = PartnerService.deletePartnerById(form.id)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        @Test
        fun `delete partner keycloak error`(@MockK(relaxed = true) partner: Partner, @MockK(relaxed = true) form: PartnerDeleteForm, @MockK expected: KeycloakIntegrationError){
            every { PartnerRepository.getPartnerByID(form.id) } returns partner.right()
            every { PartnerRepository.deletePartner(form.id) } returns Unit.right()

            every { KeycloakGroupIntegration.deleteGroup(partner.name)} returns expected.left()

            val actual = PartnerService.deletePartnerById(form.id)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

    @Nested
    inner class UpdatePartner {

        @Test
        fun `update partner success`(@MockK(relaxed = true) initial: Partner, @MockK(relaxed = true) expected: Partner) {
            val form = PartnerUpdateForm(initial.id, expected.name)

            every { PartnerRepository.getPartnerByID(expected.id) } returns initial.right()
            every { PartnerRepository.updatePartner(form) } returns expected.right()
            every {KeycloakGroupIntegration.updateGroup(initial.name, expected.name)} returns Unit.right()

            val actual = PartnerService.updatePartner(form)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `update partner repository error`(@MockK partner: Partner, @MockK(relaxed = true) form: PartnerUpdateForm, @MockK expected: RepositoryError.UpdateError){

            every { PartnerRepository.getPartnerByID(form.id) } returns partner.right()
            every { PartnerRepository.updatePartner(form) } returns expected.left()

            val actual = PartnerService.updatePartner(form)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        @Test
        fun `update partner keycloak error`(@MockK(relaxed = true) partner: Partner, @MockK(relaxed = true) form: PartnerUpdateForm, @MockK expected: KeycloakIntegrationError){

            every { PartnerRepository.getPartnerByID(form.id) } returns partner.right()
            every { PartnerRepository.updatePartner(form) } returns partner.right()
            every {KeycloakGroupIntegration.updateGroup(partner.name, form.name)} returns expected.left()

            val actual = PartnerService.updatePartner(form)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }
}