import arrow.core.Either
import io.ktor.util.*
import io.mockk.junit5.MockKExtension
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.application.service.*
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.avtale.avtaleModule
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
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(MockKExtension::class)
@Testcontainers
class KontaktTest : KoinTest {
    private val testContainer: TestContainer = TestContainer()
    private lateinit var kontaktService: IKontaktService
    private lateinit var partnerService: IPartnerService
    private lateinit var stasjonService: IStasjonService

    @BeforeAll
    fun setup() {
        testContainer.start()
        startKoin {  }
        loadKoinModules(listOf(MockAktorModule.get(), avtaleModule, hentingModule, utlysningModule, kategoriModule))
        kontaktService = get()
        partnerService = get()
        stasjonService = get()
    }

    @AfterAll
    fun tearDown() {
        testContainer.stop()
        stopKoin()
    }

    private lateinit var stasjon1: Stasjon
    private lateinit var partner1: Partner
    private lateinit var partnerKontakt1: Kontakt
    private lateinit var partnerKontakt2: Kontakt
    private lateinit var stasjonKontakt: Kontakt

    @Test
    @Order(1)
    fun setupPartnerAndStasjon() {

        val partnerInsert1 = partnerService.savePartner(PartnerSaveDto(navn = "TestPartner1", ideell = true).validateAndRequireRight())
        val stasjonInsert1 = stasjonService.save(StasjonSaveDto("TestStasjon1", StasjonType.GJENBRUK).validateAndRequireRight())

        require(partnerInsert1 is Either.Right)
        require(stasjonInsert1 is Either.Right)

        partner1 = partnerInsert1.b
        stasjon1 = stasjonInsert1.b
    }

    @Test
    @Order(2)
    fun validateWrongInput() {
        //Wrong aktorId
        KontaktSaveDto(aktorId = UUID.randomUUID(), navn = "Kontakt1").validateAndRequireLeft()
        //Empty name
        KontaktSaveDto(aktorId = partner1.id, navn = "").validateAndRequireLeft()
        //Telefon not a number
        KontaktSaveDto(aktorId = partner1.id, navn = "Kontakt1", telefon = "NotANumber").validateAndRequireLeft()
        //Telefon number too short
        KontaktSaveDto(aktorId = partner1.id, navn = "Kontakt1", telefon = "+47876543").validateAndRequireLeft()
        //Telefon number too long
        KontaktSaveDto(aktorId = partner1.id, navn = "Kontakt1", telefon = "+47987654321").validateAndRequireLeft()
        //Telefon number wrong nation code
        KontaktSaveDto(aktorId = partner1.id, navn = "Kontakt1", telefon = "+1198765432").validateAndRequireLeft()
        //Telefon number < 40000000
        KontaktSaveDto(aktorId = partner1.id, navn = "Kontakt1", telefon = "+4712345678").validateAndRequireLeft()
        //Epost invalid, no @
        KontaktSaveDto(aktorId = partner1.id, navn = "Kontakt1", epost = "not.an.email.com").validateAndRequireLeft()
        //Epost invalid, no domain
        KontaktSaveDto(aktorId = partner1.id, navn = "Kontakt1", epost = "example.email@mail").validateAndRequireLeft()
        //Epost invalid, no domain
        KontaktSaveDto(aktorId = partner1.id, navn = "Kontakt1", epost = "example.email@mail.").validateAndRequireLeft()
    }

    @Test
    @Order(3)
    fun validateCorrectInput() {
        KontaktSaveDto(aktorId = partner1.id, navn = "Kontakt1").validateAndRequireRight()
        KontaktSaveDto(aktorId = partner1.id, navn = "Kontakt1", telefon = "+4787654321").validateAndRequireRight()
        KontaktSaveDto(aktorId = partner1.id, navn = "Kontakt1", epost = "example.email@mail.com").validateAndRequireRight()
    }

    @Test
    @Order(4)
    fun createKontakt() {
        val saveDto1 = KontaktSaveDto(aktorId = partner1.id, navn = "Partner Kontakt1", telefon = "+4798765432").validateAndRequireRight()
        val saveDto2 = KontaktSaveDto(aktorId = partner1.id, navn = "Partner Kontakt2", epost = "example.email@mail.com").validateAndRequireRight()
        val saveDto3 = KontaktSaveDto(aktorId = stasjon1.id, navn = "Stasjon Kontakt", telefon = "+4798765432").validateAndRequireRight()

        val save1 = kontaktService.save(saveDto1)
        require(save1 is Either.Right)
        partnerKontakt1 = save1.b

        val save2 = kontaktService.save(saveDto2)
        require(save2 is Either.Right)
        partnerKontakt2 = save2.b

        val save3 = kontaktService.save(saveDto3)
        require(save3 is Either.Right)
        stasjonKontakt = save3.b
    }

    @Test
    @Order(5)
    fun findKontakter() {
        val findDto = KontaktGetDto(aktorId = partner1.id).validateAndRequireRight()
        val find = kontaktService.getKontakter(findDto)
        require(find is Either.Right)
        assertEquals(2, find.b.size)
        assert(find.b.containsAll(listOf(partnerKontakt1, partnerKontakt2)))
    }

    @Test
    @Order(5)
    fun updateKontakt() {
        val updateDto = KontaktUpdateDto(partnerKontakt1.id, telefon = "+4787654321", epost = "example@example.com").validateAndRequireRight()
        val update = kontaktService.update(updateDto)
        require(update is Either.Right)
        assertEquals("+4787654321", update.b.telefon)
        partnerKontakt1 = update.b
    }

    @Test
    @Order(5)
    fun deleteKontakt() {
        val delete = kontaktService.deleteKontaktById(partnerKontakt2.id)
        assert(delete is Either.Right)

        val findDto = KontaktGetDto(aktorId = partner1.id).validateAndRequireRight()
        val find = kontaktService.getKontakter(findDto)
        require(find is Either.Right)
        assertEquals(1, find.b.size)
        assert(find.b.contains(partnerKontakt1))
    }

    @Test
    @Order(5)
    fun archivePartner() {
        val archive = partnerService.archiveOne(partner1.id)
        assert(archive is Either.Right)

        val findDto = KontaktGetDto().validateAndRequireRight()
        val find = kontaktService.getKontakter(findDto)
        require(find is Either.Right)
        assertEquals(1, find.b.size)
        assert(find.b.contains(stasjonKontakt))

    }
}