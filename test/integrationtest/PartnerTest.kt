import arrow.core.Either
import arrow.core.right
import io.ktor.util.*
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.application.service.PartnerService
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.enum.PartnerStorrelse
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.infrastructure.repository.PartnerRepository
import ombruk.backend.shared.api.KeycloakGroupIntegration
import org.junit.jupiter.api.*
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(MockKExtension::class)
@Testcontainers
class PartnerTest {
    private val testContainer: TestContainer = TestContainer()
    private lateinit var partnerService: PartnerService
    private var partnerRepository = PartnerRepository()
    private var keycloakGroupIntegration = mockkClass(KeycloakGroupIntegration::class)

    @OptIn(KtorExperimentalAPI::class)
    @BeforeAll
    fun setup() {
        testContainer.start()
        partnerService = PartnerService(keycloakGroupIntegration, partnerRepository)//PartnerService(partnerRepository, keycloakGroupIntegration)
    }

    private lateinit var navn: String
    private lateinit var storrelse: PartnerStorrelse
    private var ideell: Boolean = false
    private lateinit var uuid: UUID
    private lateinit var updateNavn: String
    private lateinit var updateStorrelse: PartnerStorrelse
    private lateinit var updateType: StasjonType
    private var updateIdeell: Boolean = true

    @Test
    @Order(1)
    fun testInsert(@MockK expected: Any) {
        navn = "Nesferg"
        storrelse = PartnerStorrelse.LITEN
        ideell = false
        every { keycloakGroupIntegration.createGroup(navn, any<UUID>()) } returns expected.right()

        val partner = PartnerInsertDto(navn, storrelse, ideell)
        val save = partnerService.savePartner(partner)
        assert(save is Either.Right<Partner>)
    }

    @Test
    @Order(2)
    fun testFind() {
        val partner = PartnerGetDto(navn)
        val find = partnerService.getPartnere(partner)
        require(find is Either.Right)
        assertTrue(find.b.count() == 1)
        assertEquals(navn, find.b[0].navn)
        assertEquals(storrelse, find.b[0].storrelse)
        assertEquals(ideell, find.b[0].ideell)
        // UUID for next test
        uuid = find.b[0].id
    }

    @Test
    @Order(3)
    fun testFindOne() {
        val findOne = partnerService.getPartnerById(uuid)
        require(findOne is Either.Right)
        assertEquals(uuid, findOne.b.id)
        assertEquals(navn, findOne.b.navn)
        assertEquals(storrelse, findOne.b.storrelse)
        assertEquals(ideell, findOne.b.ideell)
    }

    @OptIn(KtorExperimentalAPI::class)
    @Test
    @Order(4)
    fun testUpdate(@MockK expected: Unit) {
        updateNavn = "Nesferg Middels"
        updateIdeell = true
        updateStorrelse = PartnerStorrelse.MIDDELS
        every { keycloakGroupIntegration.updateGroup(navn, updateNavn) } returns expected.right()

        val partner = PartnerUpdateDto(uuid, updateNavn, updateIdeell, updateStorrelse)
        val update = partnerService.updatePartner(partner)
        require(update is Either.Right)
        assertEquals(updateNavn, update.b.navn)
        assertEquals(updateIdeell, update.b.ideell)
        assertEquals(updateStorrelse, update.b.storrelse)
    }

    @Test
    @Order(5)
    fun testDelete(@MockK expected: Any) {
        every { keycloakGroupIntegration.deleteGroup(updateNavn) } returns expected.right()

        val update = partnerService.deletePartnerById(uuid)
        assert(update is Either.Right)
    }

    @Test
    @Order(6)
    fun testFindOneFails() {
        val findOne = partnerService.getPartnerById(uuid)
        assert(findOne is Either.Left)
    }
}