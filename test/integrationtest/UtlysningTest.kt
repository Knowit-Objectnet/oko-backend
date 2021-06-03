import arrow.core.Either
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.application.api.dto.PartnerSaveDto
import ombruk.backend.aktor.application.api.dto.StasjonSaveDto
import ombruk.backend.aktor.application.service.PartnerService
import ombruk.backend.aktor.application.service.StasjonService
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.enum.PartnerStorrelse
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.infrastructure.repository.PartnerRepository
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.avtale.application.api.dto.AvtaleFindDto
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.henting.application.api.dto.EkstraHentingFindDto
import ombruk.backend.henting.application.api.dto.EkstraHentingSaveDto
import ombruk.backend.henting.application.service.EkstraHentingService
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.infrastructure.repository.EkstraHentingRepository
import ombruk.backend.shared.api.KeycloakGroupIntegration
import ombruk.backend.utlysning.application.api.dto.UtlysningBatchPostDto
import ombruk.backend.utlysning.application.service.UtlysningService
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.infrastructure.repository.UtlysningRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(MockKExtension::class)
@Testcontainers
class UtlysningTest {
    private val testContainer = TestContainer()
    private val partnerRepository = PartnerRepository()
    private val stasjonRepository = StasjonRepository()
    private val ekstraHentingRepository = EkstraHentingRepository()
    private val ekstraHentingService = EkstraHentingService(ekstraHentingRepository)
    private val utlysningRepository = UtlysningRepository()
    private val utlysningService = UtlysningService(utlysningRepository)
    private lateinit var partnerService: PartnerService
    private lateinit var stasjonService: StasjonService
    private var keycloakGroupIntegration = mockkClass(KeycloakGroupIntegration::class)

    @BeforeAll
    fun setup() {
        testContainer.start()
        stasjonService = StasjonService(stasjonRepository, keycloakGroupIntegration)
        partnerService = PartnerService(keycloakGroupIntegration, partnerRepository)
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

        val partnerInsert1 = partnerService.savePartner(PartnerSaveDto("TestPartner1", PartnerStorrelse.STOR, true))
        val partnerInsert2 = partnerService.savePartner(PartnerSaveDto("TestPartner2", PartnerStorrelse.MIDDELS, true))
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

        val ehSave = ekstraHentingService.create(ekstraHentingSaveDto)
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
        val utlysningBatchPostDto = UtlysningBatchPostDto(
            hentingId = ekstraHenting.id,
            partnerIds = listOf(partner1.id, partner2.id)
        )

        val batchPost = utlysningService.batchCreate(utlysningBatchPostDto)
        require(batchPost is Either.Right)
        utlysninger1 = batchPost.b

        assert(utlysninger1.any { it.partnerId == partner1.id })
        assert(utlysninger1.any { it.partnerId == partner2.id })

    }


    /*TODO:
    * Test accepting of Utlysning
    * Get all currently accepted Utlysning
    * Stasjon confirm Utlysning
    * ...
    * */

}