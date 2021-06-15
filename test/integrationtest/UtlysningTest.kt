import arrow.core.Either
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.aktorModule
import ombruk.backend.aktor.application.api.dto.PartnerSaveDto
import ombruk.backend.aktor.application.api.dto.StasjonSaveDto
import ombruk.backend.aktor.application.service.IPartnerService
import ombruk.backend.aktor.application.service.IStasjonService
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.avtale.avtaleModule
import ombruk.backend.henting.application.api.dto.EkstraHentingFindDto
import ombruk.backend.henting.application.api.dto.EkstraHentingSaveDto
import ombruk.backend.henting.application.service.IEkstraHentingService
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.hentingModule
import ombruk.backend.kategori.kategoriModule
import ombruk.backend.shared.api.KeycloakGroupIntegration
import ombruk.backend.utlysning.application.api.dto.UtlysningBatchSaveDto
import ombruk.backend.utlysning.application.api.dto.UtlysningPartnerAcceptDto
import ombruk.backend.utlysning.application.service.IUtlysningService
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.utlysningModule
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(MockKExtension::class)
@Testcontainers
class UtlysningTest : KoinTest {
    private val testContainer = TestContainer()
    private lateinit var ekstraHentingService: IEkstraHentingService
    private lateinit var utlysningService: IUtlysningService
    private lateinit var partnerService: IPartnerService
    private lateinit var stasjonService: IStasjonService
    private var keycloakGroupIntegration = mockkClass(KeycloakGroupIntegration::class)

    @BeforeAll
    fun setup() {
        testContainer.start()
        startKoin {  }
        loadKoinModules(listOf(hentingModule, aktorModule, utlysningModule, avtaleModule, kategoriModule))
        stasjonService = get()
        partnerService = get()
        ekstraHentingService = get()
        utlysningService = get()
    }

    @AfterAll
    fun tearDown() {
        stopKoin()
    }

    private lateinit var stasjon1: Stasjon
    private lateinit var stasjon2: Stasjon
    private lateinit var partner1: Partner
    private lateinit var partner2: Partner
    private lateinit var ekstraHenting: EkstraHenting
    private lateinit var utlysninger1: List<Utlysning>

    @Test
    @Order(1)
    fun setupPartnerAndStasjon(@MockK expected: Any) {

        every { keycloakGroupIntegration.createGroup(any<String>(), any<UUID>()) } returns expected.right()

        val partnerInsert1 = partnerService.savePartner(PartnerSaveDto("TestPartner1", true))
        val partnerInsert2 = partnerService.savePartner(PartnerSaveDto("TestPartner2", true))
        val stasjonInsert1 = stasjonService.save(StasjonSaveDto("TestStasjon1", StasjonType.GJENBRUK))
        val stasjonInsert2 = stasjonService.save(StasjonSaveDto("TestStasjon2", StasjonType.MINI))

        require(partnerInsert1 is Either.Right)
        require(partnerInsert2 is Either.Right)
        require(stasjonInsert1 is Either.Right)
        require(stasjonInsert2 is Either.Right)

        partner1 = partnerInsert1.b
        partner2 = partnerInsert2.b
        stasjon1 = stasjonInsert1.b
        stasjon2 = stasjonInsert2.b
    }

    @Test
    @Order(2)
    fun createEkstraHenting(){
        val ekstraHentingSaveDto = EkstraHentingSaveDto(
            startTidspunkt = LocalDateTime.of(2021,6,3,10,0),
            sluttTidspunkt = LocalDateTime.of(2021,6,3,12,30),
            merknad = null,
            stasjonId = stasjon1.id
        )

        val ehSave = ekstraHentingService.save(ekstraHentingSaveDto)
        require(ehSave is Either.Right)

        ekstraHenting = ehSave.b
    }

    @Test
    @Order(3)
    fun testFindAfterInsert() {
        val findOne = ekstraHentingService.findOne(ekstraHenting.id)
        require(findOne is Either.Right<EkstraHenting>)

        val find = ekstraHentingService.find(EkstraHentingFindDto())
        require(find is Either.Right<List<EkstraHenting>>)

        assertEquals(ekstraHenting, findOne.b)
        assert(find.b.size == 1)
        assertEquals(ekstraHenting, find.b[0])
    }

    @Test
    @Order(4)
    fun testAddUtlysninger() {
        val utlysningBatchPostDto = UtlysningBatchSaveDto(
            hentingId = ekstraHenting.id,
            partnerIds = listOf(partner1.id, partner2.id)
        )

        val batchPost = utlysningService.batchSave(utlysningBatchPostDto)
        require(batchPost is Either.Right)
        utlysninger1 = batchPost.b

        assert(utlysninger1.any { it.partnerId == partner1.id })
        assert(utlysninger1.any { it.partnerId == partner2.id })

    }

    @Test
    @Order(5)
    fun testAcceptUtlysning() {

        //Tests the partner accept functionality (stasjonAccept currently not in use because of first-pass-the-post).
        //End result should be that utlysninger1[1] is accepted.

        val partnerAcceptDto1 = UtlysningPartnerAcceptDto(utlysninger1[0].id, true).validOrError()
        require(partnerAcceptDto1 is Either.Right)

        val accepted1 = utlysningService.partnerAccept(partnerAcceptDto1.b)
        println(accepted1)
        require(accepted1 is Either.Right)
        assertNotNull(accepted1.b.partnerPameldt)

        val partnerAcceptDto2 = UtlysningPartnerAcceptDto(utlysninger1[1].id, true).validOrError()
        require(partnerAcceptDto2 is Either.Left)

        val partnerAcceptDto3 = UtlysningPartnerAcceptDto(utlysninger1[0].id, false).validOrError()
        require(partnerAcceptDto3 is Either.Right)

        val unAccept = utlysningService.partnerAccept(partnerAcceptDto3.b)
        require(unAccept is Either.Right)
        assertNull(unAccept.b.partnerPameldt)

        val partnerAcceptDto4 = UtlysningPartnerAcceptDto(utlysninger1[1].id, true).validOrError()
        require(partnerAcceptDto4 is Either.Right)

        val accepted2 = utlysningService.partnerAccept(partnerAcceptDto4.b)
        require(accepted2 is Either.Right)
        assertNotNull(accepted2.b.partnerPameldt)
    }

}