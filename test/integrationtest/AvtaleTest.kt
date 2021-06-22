import arrow.core.Either
import avtale.application.api.dto.AvtaleSaveDto
import henting.application.api.dto.HenteplanSaveDto
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import ombruk.backend.aktor.application.api.dto.PartnerSaveDto
import ombruk.backend.aktor.application.api.dto.StasjonSaveDto
import ombruk.backend.aktor.application.service.IPartnerService
import ombruk.backend.aktor.application.service.IStasjonService
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.avtale.application.api.dto.AvtaleFindDto
import ombruk.backend.avtale.application.service.IAvtaleService
import ombruk.backend.avtale.avtaleModule
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.henting.application.api.dto.PlanlagtHentingFindDto
import ombruk.backend.henting.application.service.IHenteplanService
import ombruk.backend.henting.application.service.IPlanlagtHentingService
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.hentingModule
import ombruk.backend.kategori.kategoriModule
import ombruk.backend.utlysning.utlysningModule
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(MockKExtension::class)
@Testcontainers
class AvtaleTest : KoinTest {
    private val testContainer = TestContainer()
    private lateinit var avtaleService: IAvtaleService
    private lateinit var planlagtHentingService: IPlanlagtHentingService
    private lateinit var henteplanService: IHenteplanService
    private lateinit var partnerService: IPartnerService
    private lateinit var stasjonService: IStasjonService

    @BeforeAll
    fun setup() {
        testContainer.start()
        startKoin {}
        loadKoinModules(listOf(avtaleModule, MockAktorModule.get(), hentingModule, utlysningModule, kategoriModule))
        avtaleService = get()
        stasjonService = get()
        partnerService = get()
        planlagtHentingService = get()
        henteplanService = get()
    }

    @AfterAll
    fun tearDown() {
        testContainer.stop()
        stopKoin()
    }

    private lateinit var avtale: Avtale
    private lateinit var stasjon: Stasjon
    private lateinit var partner: Partner
    private lateinit var henteplan1: Henteplan
    private lateinit var henteplan2: Henteplan

    @Test
    @Order(1)
    fun setupPartnerAndStasjon(@MockK expected: Any) {

        val partnerInsert = partnerService.savePartner(PartnerSaveDto(navn = "TestPartner", ideell = true).validateAndRequireRight())
        val stasjonInsert = stasjonService.save(StasjonSaveDto("TestStasjon", StasjonType.GJENBRUK).validateAndRequireRight())

        require(partnerInsert is Either.Right)
        require(stasjonInsert is Either.Right)

        partner = partnerInsert.b
        stasjon = stasjonInsert.b
    }

    @Test
    @Order(2)
    fun createANewAvtale() {
        val avtaleCreateDto = AvtaleSaveDto(
            partner.id,
            AvtaleType.FAST,
            LocalDate.of(2021,1,1),
            LocalDate.of(2022,1,1),
            emptyList()
        ).validateAndRequireRight()

        val avtaleEither = avtaleService.save(avtaleCreateDto)

        assert(avtaleEither is Either.Right<Avtale>)
        require(avtaleEither is Either.Right<Avtale>)
        avtale = avtaleEither.b

    }

    @Test()
    @Order(2)
    fun createANewAvtaleWithWrongPartnerId() {
        val avtaleCreateDto = AvtaleSaveDto(
            UUID.randomUUID(),
            AvtaleType.FAST,
            LocalDate.of(2021,1,1),
            LocalDate.of(2022,1,1),
            emptyList()
        ).validOrError()

        assert(avtaleCreateDto is Either.Left)
    }

    @Test
    @Order(3)
    fun testFindAfterInsert() {
        val findOne = avtaleService.findOne(avtale.id)
        require(findOne is Either.Right<Avtale>)

        val find = avtaleService.find(AvtaleFindDto().validateAndRequireRight())
        require(find is Either.Right<List<Avtale>>)

        assertEquals(avtale, findOne.b)
        assert(find.b.size == 1)
        assertEquals(avtale, find.b[0])
    }

    @Test
    @Order(4)
    fun testAddHenteplaner() {
        val henteplanPostDto1 = HenteplanSaveDto(
            avtale.id,
            stasjon.id,
            HenteplanFrekvens.UKENTLIG,
            LocalDateTime.of(2021,1,1,10,0),
            LocalDateTime.of(2021,2,1,14,0),
            DayOfWeek.FRIDAY,
            null
        ).validateAndRequireRight()

        val henteplanPostDto2 = HenteplanSaveDto(
            avtale.id,
            stasjon.id,
            HenteplanFrekvens.ENKELT,
            LocalDateTime.of(2021,1,1,10,0),
            LocalDateTime.of(2021,1,1,14,0),
            DayOfWeek.FRIDAY,
            null
        ).validateAndRequireRight()

        val henteplanCreate1 = henteplanService.save(henteplanPostDto1)
        require(henteplanCreate1 is Either.Right)
        henteplan1 = henteplanCreate1.b
        assert(!henteplan1.planlagteHentinger.isNullOrEmpty())

        val henteplanCreate2 = henteplanService.save(henteplanPostDto2)
        require(henteplanCreate2 is Either.Right)
        henteplan2 = henteplanCreate2.b
        assert(!henteplan2.planlagteHentinger.isNullOrEmpty())
    }

    @Test
    @Order(5)
    fun testFindAvtaleWithHenteplan() {
        val findOne = avtaleService.findOne(avtale.id)
        require(findOne is Either.Right<Avtale>)

        assert(findOne.b.henteplaner.size == 2)
        //Planlagt Henting should not be returned when getting through Avtale
        findOne.b.henteplaner.map { assert(it.planlagteHentinger.isNullOrEmpty()) }

    }

    @Test
    @Order(6)
    fun testGetAllHentingInTimeframe() {
        val findAll = planlagtHentingService.find(PlanlagtHentingFindDto(
            after = henteplan1.planlagteHentinger!![0].startTidspunkt.minusHours(1),
            before = henteplan1.planlagteHentinger!![3].startTidspunkt.plusHours(1)
        ).validateAndRequireRight())

        require(findAll is Either.Right)

        assert(findAll.b.containsAll(henteplan1.planlagteHentinger!!.subList(0,3)))
        assert(findAll.b.containsAll(henteplan2.planlagteHentinger!!))
        assert(findAll.b.size == 4)
    }

    //TODO: Make update tests

    //Avtaler, Hentinger, etc should be cancelled, not deleted, so delete tests unneeded, though cancellation tests necessary

}
