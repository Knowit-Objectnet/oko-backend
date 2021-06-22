import arrow.core.Either
import io.mockk.junit5.MockKExtension
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
import ombruk.backend.utlysning.application.api.dto.UtlysningBatchSaveDto
import ombruk.backend.utlysning.application.api.dto.UtlysningFindDto
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
import testutils.MockAktorModule
import testutils.TestContainer
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    @BeforeAll
    fun setup() {
        testContainer.start()
        startKoin {  }
        loadKoinModules(listOf(hentingModule, MockAktorModule.get(), utlysningModule, avtaleModule, kategoriModule))
        stasjonService = get()
        partnerService = get()
        ekstraHentingService = get()
        utlysningService = get()
    }

    @AfterAll
    fun tearDown() {
        testContainer.stop()
        stopKoin()
    }

    private lateinit var stasjon1: Stasjon
    private lateinit var stasjon2: Stasjon
    private lateinit var partner1: Partner
    private lateinit var partner2: Partner
    private lateinit var partner3: Partner
    private lateinit var ekstraHenting: EkstraHenting
    private lateinit var utlysninger1: List<Utlysning>

    @Test
    @Order(1)
    fun setupPartnerAndStasjon() {

        val partnerInsert1 = partnerService.savePartner(PartnerSaveDto(navn = "TestPartner1", ideell = true))
        val partnerInsert2 = partnerService.savePartner(PartnerSaveDto(navn = "TestPartner2", ideell = true))
        val partnerInsert3 = partnerService.savePartner(PartnerSaveDto(navn = "TestPartner3", ideell = true))
        val stasjonInsert1 = stasjonService.save(StasjonSaveDto("TestStasjon1", StasjonType.GJENBRUK))
        val stasjonInsert2 = stasjonService.save(StasjonSaveDto("TestStasjon2", StasjonType.MINI))

        require(partnerInsert1 is Either.Right)
        require(partnerInsert2 is Either.Right)
        require(partnerInsert3 is Either.Right)
        require(stasjonInsert1 is Either.Right)
        require(stasjonInsert2 is Either.Right)

        partner1 = partnerInsert1.b
        partner2 = partnerInsert2.b
        partner3 = partnerInsert3.b
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
        ).validOrError()
        require(ekstraHentingSaveDto is Either.Right)

        val ehSave = ekstraHentingService.save(ekstraHentingSaveDto.b)
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
            partnerIds = listOf(partner1.id.toString(), partner2.id.toString())
        ).validOrError()
        require(utlysningBatchPostDto is Either.Right)

        val utlysningBatchPostDtoWrong1 = UtlysningBatchSaveDto(
            hentingId = ekstraHenting.id,
            partnerIds = listOf(partner1.id.toString(), "This is not a legal UUID")
        ).validOrError()
        assert(utlysningBatchPostDtoWrong1 is Either.Left)

        val utlysningBatchPostDtoWrong2 = UtlysningBatchSaveDto(
            hentingId = ekstraHenting.id,
            partnerIds = listOf(partner1.id.toString(), UUID.randomUUID().toString())
        ).validOrError()
        assert(utlysningBatchPostDtoWrong2 is Either.Left)

        val batchPost = utlysningService.batchSave(utlysningBatchPostDto.b)
        require(batchPost is Either.Right)
        utlysninger1 = batchPost.b

        assert(utlysninger1.any { it.partnerId == partner1.id })
        assert(utlysninger1.any { it.partnerId == partner2.id })

    }

    @Test
    @Order(4)
    fun testAddMoreUtlysninger() {
        assertEquals(2, utlysninger1.size)

        val utlysningBatchSaveDto1 = UtlysningBatchSaveDto(
            hentingId = ekstraHenting.id,
            partnerIds = listOf(partner1.id.toString(), partner2.id.toString())
        ).validOrError()
        require(utlysningBatchSaveDto1 is Either.Right)

        val batchPostSame = utlysningService.batchSave(utlysningBatchSaveDto1.b)
        require(batchPostSame is Either.Right)
        assertEquals(0, batchPostSame.b.size)

        val utlysningBatchSaveDto2 = UtlysningBatchSaveDto(
            hentingId = ekstraHenting.id,
            partnerIds = listOf(partner1.id.toString(), partner2.id.toString(), partner3.id.toString())
        ).validOrError()
        require(utlysningBatchSaveDto2 is Either.Right)

        val batchPostSameAndNew = utlysningService.batchSave(
            utlysningBatchSaveDto2.b
        )
        require(batchPostSameAndNew is Either.Right)
        assertEquals(1, batchPostSameAndNew.b.size)


        val utlysningFindDto = UtlysningFindDto(hentingId = ekstraHenting.id).validOrError()
        require(utlysningFindDto is Either.Right)

        val findUtlysninger = utlysningService.find(utlysningFindDto.b)
        require(findUtlysninger is Either.Right)
        utlysninger1 = findUtlysninger.b

        assert(utlysninger1.any { it.partnerId == partner1.id })
        assert(utlysninger1.any { it.partnerId == partner2.id })
        assert(utlysninger1.any { it.partnerId == partner3.id })
    }

    @Test
    @Order(6)
    fun testFindNoneAccepted() {
        //Assert that none have been accepted yet, and that this shows in both utlysninger and EkstraHenting:

        //Utlysning
        assertFalse(utlysninger1.any { it.partnerPameldt != null })

        //EkstraHenting
        val findOne = ekstraHentingService.findOne(ekstraHenting.id)
        require(findOne is Either.Right)
        assertNull( findOne.b.godkjentUtlysning )
    }

    lateinit var acceptedUtlysning: Utlysning

    @Test
    @Order(7)
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
        acceptedUtlysning = accepted2.b
    }

    @Test
    @Order(8)
    fun testFindAccepted() {
        //Assert that none have been accepted yet, and that this shows in both utlysninger and EkstraHenting:

        //Utlysning
        val utlysningFindDto = UtlysningFindDto(hentingId = ekstraHenting.id).validOrError()
        require(utlysningFindDto is Either.Right)

        val findUtlysninger = utlysningService.find(utlysningFindDto.b)
        require(findUtlysninger is Either.Right)
        utlysninger1 = findUtlysninger.b

        assertTrue(utlysninger1.any { it.partnerPameldt != null })

        //EkstraHenting
        val findOne = ekstraHentingService.findOne(ekstraHenting.id)
        require(findOne is Either.Right)
        assertNotNull( findOne.b.godkjentUtlysning )
        assertEquals(acceptedUtlysning ,findOne.b.godkjentUtlysning)
    }

}