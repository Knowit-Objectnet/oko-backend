import arrow.core.Either
import arrow.core.right
import io.ktor.util.*
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.aktorModule
import ombruk.backend.aktor.application.api.dto.PartnerGetDto
import ombruk.backend.aktor.application.api.dto.PartnerSaveDto
import ombruk.backend.aktor.application.api.dto.PartnerUpdateDto
import ombruk.backend.aktor.application.service.IKontaktService
import ombruk.backend.aktor.application.service.IPartnerService
import ombruk.backend.aktor.application.service.KontaktService
import ombruk.backend.aktor.application.service.PartnerService
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.infrastructure.repository.PartnerRepository
import ombruk.backend.avtale.avtaleModule
import ombruk.backend.henting.hentingModule
import ombruk.backend.kategori.kategoriModule
import ombruk.backend.shared.api.KeycloakGroupIntegration
import ombruk.backend.utlysning.utlysningModule
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(MockKExtension::class)
@Testcontainers
class PartnerTest : KoinTest {
    private val testContainer: TestContainer = TestContainer()
    private lateinit var partnerService: IPartnerService
    private var keycloakGroupIntegration = mockkClass(KeycloakGroupIntegration::class)

    @BeforeAll
    fun setup() {
        testContainer.start()
        startKoin {  }
        loadKoinModules(listOf(aktorModule, avtaleModule, hentingModule, utlysningModule, kategoriModule))
        partnerService = get()
    }

    @AfterAll
    fun tearDown() {
        testContainer.stop()
        stopKoin()
    }

    private lateinit var navn: String
    private var ideell: Boolean = false
    private lateinit var uuid: UUID
    private lateinit var updateNavn: String
    private lateinit var updateType: StasjonType
    private var updateIdeell: Boolean = true

    @Test
    @Order(1)
    fun testInsert(@MockK expected: Any) {
        navn = "Nesferg"
        ideell = false
        every { keycloakGroupIntegration.createGroup(navn, any<UUID>()) } returns expected.right()

        val partner = PartnerSaveDto(navn, ideell)
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
        assertEquals(ideell, findOne.b.ideell)
    }

    @OptIn(KtorExperimentalAPI::class)
    @Test
    @Order(4)
    fun testUpdate(@MockK expected: Unit) {
        updateNavn = "Nesferg Middels"
        updateIdeell = true
        every { keycloakGroupIntegration.updateGroup(navn, updateNavn) } returns expected.right()

        val partner = PartnerUpdateDto(uuid, updateNavn, updateIdeell)
        val update = partnerService.updatePartner(partner)
        require(update is Either.Right)
        assertEquals(updateNavn, update.b.navn)
        assertEquals(updateIdeell, update.b.ideell)
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