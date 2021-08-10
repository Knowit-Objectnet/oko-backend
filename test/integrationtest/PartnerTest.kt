import arrow.core.Either
import io.ktor.util.*
import io.mockk.junit5.MockKExtension
import ombruk.backend.aarsak.aarsakModule
import ombruk.backend.aktor.application.api.dto.PartnerGetDto
import ombruk.backend.aktor.application.api.dto.PartnerSaveDto
import ombruk.backend.aktor.application.api.dto.PartnerUpdateDto
import ombruk.backend.aktor.application.service.IPartnerService
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.avtale.avtaleModule
import ombruk.backend.henting.hentingModule
import ombruk.backend.kategori.kategoriModule
import ombruk.backend.utlysning.utlysningModule
import ombruk.backend.vektregistrering.vektregistreringModule
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.MockAktorModule
import testutils.TestContainer
import testutils.validateAndRequireLeft
import testutils.validateAndRequireRight
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

    @BeforeAll
    fun setup() {
        testContainer.start()
        startKoin {  }
        loadKoinModules(listOf(MockAktorModule.get(), avtaleModule, hentingModule, utlysningModule, kategoriModule, vektregistreringModule, aarsakModule))
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
    fun testInsert() {
        navn = "Nesferg"
        ideell = false

        val partner = PartnerSaveDto(navn = navn, ideell = ideell).validateAndRequireRight()
        val save = partnerService.savePartner(partner)
        assert(save is Either.Right<Partner>)
    }

    @Test
    @Order(2)
    fun testFind() {
        val partner = PartnerGetDto(navn).validateAndRequireRight()
        val find = partnerService.getPartnere(partner, false)
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
        val findOne = partnerService.getPartnerById(uuid, false)
        require(findOne is Either.Right)
        assertEquals(uuid, findOne.b.id)
        assertEquals(navn, findOne.b.navn)
        assertEquals(ideell, findOne.b.ideell)
    }

    @OptIn(KtorExperimentalAPI::class)
    @Test
    @Order(4)
    fun testUpdate() {
        updateNavn = "Nesferg Middels"
        updateIdeell = true

        val partner = PartnerUpdateDto(uuid, updateNavn, updateIdeell).validateAndRequireRight()
        val update = partnerService.updatePartner(partner)
        require(update is Either.Right)
        assertEquals(updateNavn, update.b.navn)
        assertEquals(updateIdeell, update.b.ideell)
    }

    @Test
    @Order(5)
    fun testDelete() {
        val update = partnerService.deletePartnerById(uuid)
        assert(update is Either.Right)
    }

    @Test
    @Order(6)
    fun testFindOneFails() {
        val findOne = partnerService.getPartnerById(uuid, false)
        assert(findOne is Either.Left)
    }
}