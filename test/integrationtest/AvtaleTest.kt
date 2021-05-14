import arrow.core.Either
import arrow.core.contains
import arrow.core.right
import avtale.application.api.dto.AvtaleCreateDto
import henting.application.api.dto.HenteplanPostDto
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.application.api.dto.PartnerPostDto
import ombruk.backend.aktor.application.api.dto.StasjonCreateDto
import ombruk.backend.aktor.application.service.PartnerService
import ombruk.backend.aktor.application.service.StasjonService
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.enum.PartnerStorrelse
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.infrastructure.repository.PartnerRepository
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.avtale.application.api.dto.AvtaleDeleteDto
import ombruk.backend.avtale.application.api.dto.AvtaleFindDto
import ombruk.backend.avtale.application.service.AvtaleService
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.infrastructure.repository.AvtaleRepository
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.henting.application.api.dto.HenteplanDeleteDto
import ombruk.backend.henting.application.api.dto.PlanlagtHentingDeleteDto
import ombruk.backend.henting.application.api.dto.PlanlagtHentingFindDto
import ombruk.backend.henting.application.service.HenteplanService
import ombruk.backend.henting.application.service.PlanlagtHentingService
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.entity.Henting
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.infrastructure.repository.HenteplanRepository
import ombruk.backend.henting.infrastructure.repository.PlanlagtHentingRepository
import ombruk.backend.shared.api.KeycloakGroupIntegration
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(MockKExtension::class)
@Testcontainers
class AvtaleTest {
    private val testContainer = TestContainer()
    private lateinit var avtaleService: AvtaleService
    private val henteplanRepository = HenteplanRepository()
    private val planlagtHentingRepository = PlanlagtHentingRepository()
    private val planlagtHentingService = PlanlagtHentingService(planlagtHentingRepository)
    private val henteplanService = HenteplanService(henteplanRepository, planlagtHentingService)
    private val avtaleRepository = AvtaleRepository()
    private val partnerRepository = PartnerRepository()
    private val stasjonRepository = StasjonRepository()
    private lateinit var partnerService: PartnerService
    private lateinit var stasjonService: StasjonService
    private var keycloakGroupIntegration = mockkClass(KeycloakGroupIntegration::class)

    @BeforeAll
    fun setup() {
        testContainer.start()
        avtaleService = AvtaleService(avtaleRepository,henteplanService)
        stasjonService = StasjonService(stasjonRepository, keycloakGroupIntegration)
        partnerService = PartnerService(keycloakGroupIntegration, partnerRepository)
    }

    private lateinit var avtale: Avtale
    private lateinit var stasjon: Stasjon
    private lateinit var partner: Partner
    private lateinit var henteplan1: Henteplan
    private lateinit var henteplan2: Henteplan

    @Test
    @Order(1)
    fun setupPartnerAndStasjon(@MockK expected: Any) {

        every { keycloakGroupIntegration.createGroup(any<String>(), any<UUID>()) } returns expected.right()

        val partnerInsert = partnerService.savePartner(PartnerPostDto("TestPartner", PartnerStorrelse.STOR, true))
        val stasjonInsert = stasjonService.save(StasjonCreateDto("TestStasjon", StasjonType.GJENBRUK))

        require(partnerInsert is Either.Right)
        require(stasjonInsert is Either.Right)

        partner = partnerInsert.b
        stasjon = stasjonInsert.b
    }

    @Test
    @Order(2)
    fun createANewAvtale() {
        val avtaleCreateDto = AvtaleCreateDto(
            UUID.randomUUID(), //TODO: This needs to verify that it is a legal Aktor
            AvtaleType.FAST,
            LocalDate.of(2021,1,1),
            LocalDate.of(2022,1,1),
            emptyList()
        )

        val avtaleEither = avtaleService.save(avtaleCreateDto)

        assert(avtaleEither is Either.Right<Avtale>)
        require(avtaleEither is Either.Right<Avtale>)
        avtale = avtaleEither.b

    }

    @Test
    @Order(3)
    fun testFindAfterInsert() {
        val findOne = avtaleService.findOne(avtale.id)
        require(findOne is Either.Right<Avtale>)

        val find = avtaleService.find(AvtaleFindDto())
        require(find is Either.Right<List<Avtale>>)

        assertEquals(avtale, findOne.b)
        assert(find.b.size == 1)
        assertEquals(avtale, find.b[0])
    }

    @Test
    @Order(4)
    fun testAddHenteplaner() {
        val henteplanPostDto1 = HenteplanPostDto(
            avtale.id,
            stasjon.id,
            HenteplanFrekvens.Ukentlig,
            LocalDateTime.of(2021,1,1,10,0),
            LocalDateTime.of(2021,2,1,14,0),
            DayOfWeek.FRIDAY,
            null
        )

        val henteplanPostDto2 = HenteplanPostDto(
            avtale.id,
            stasjon.id,
            HenteplanFrekvens.Enkelt,
            LocalDateTime.of(2021,1,1,10,0),
            LocalDateTime.of(2021,1,1,14,0),
            DayOfWeek.FRIDAY,
            null
        )

        val henteplanCreate1 = henteplanService.create(henteplanPostDto1)
        require(henteplanCreate1 is Either.Right)
        henteplan1 = henteplanCreate1.b
        assert(!henteplan1.planlagteHentinger.isNullOrEmpty())

        val henteplanCreate2 = henteplanService.create(henteplanPostDto2)
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
        ))

        require(findAll is Either.Right)

        assert(findAll.b.containsAll(henteplan1.planlagteHentinger!!.subList(0,3)))
        assert(findAll.b.containsAll(henteplan2.planlagteHentinger!!))
        assert(findAll.b.size == 4)
    }

    //TODO: Make update tests

    //Avtaler, Hentinger, etc should be cancelled, not deleted, so delete tests unneeded, though cancellation tests necessary

}
