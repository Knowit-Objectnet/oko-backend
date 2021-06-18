import arrow.core.Either
import io.ktor.util.*
import io.mockk.junit5.MockKExtension
import ombruk.backend.aktor.application.api.dto.StasjonFindDto
import ombruk.backend.aktor.application.api.dto.StasjonSaveDto
import ombruk.backend.aktor.application.api.dto.StasjonUpdateDto
import ombruk.backend.aktor.application.service.IStasjonService
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
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(MockKExtension::class)
@Testcontainers
class StasjonTest : KoinTest{
    private val testContainer: TestContainer = TestContainer()
    private lateinit var stasjonService: IStasjonService

    @OptIn(KtorExperimentalAPI::class)
    @BeforeAll
    fun setup() {
        testContainer.start()
        startKoin {  }
        loadKoinModules(listOf(MockAktorModule.get(), avtaleModule, hentingModule, utlysningModule, kategoriModule))
        stasjonService = get()
    }

    @AfterAll
    fun tearDown() {
        stopKoin()
    }

    private lateinit var navn: String
    private lateinit var type: StasjonType
    private lateinit var uuid: UUID
    private lateinit var updateNavn: String
    private lateinit var updateType: StasjonType

    @Test
    @Order(1)
    fun testInsert() {
        navn = "TestStasjon"
        type = StasjonType.GJENBRUK

        val stasjon = StasjonSaveDto(navn, type)
        val save = stasjonService.save(stasjon)
        assert(save is Either.Right<Stasjon>)
    }

    @Test
    @Order(2)
    fun testFind() {
        val stasjon = StasjonFindDto(navn)
        val find = stasjonService.find(stasjon, false)
        require(find is Either.Right)
        assertEquals(1, find.b.count())
        assertEquals(navn, find.b[0].navn)
        assertEquals(type, find.b[0].type)
        // UUID for next test
        uuid = find.b[0].id
    }

    @Test
    @Order(3)
    fun testFindOne() {
        val findOne = stasjonService.findOne(uuid, false)
        require(findOne is Either.Right)
        assertEquals(uuid, findOne.b.id)
        assertEquals(navn, findOne.b.navn)
        assertEquals(type, findOne.b.type)
    }

    @OptIn(KtorExperimentalAPI::class)
    @Test
    @Order(4)
    fun testUpdate() {
        updateNavn = "Grefsen Mini"
        updateType = StasjonType.MINI

        val stasjon = StasjonUpdateDto(uuid, updateNavn, updateType)
        val update = stasjonService.update(stasjon)
        require(update is Either.Right)
        assertNotEquals(navn, update.b.navn)
        assertEquals(updateNavn, update.b.navn)
        assertEquals(updateType, update.b.type)
    }

    @Test
    @Order(5)
    fun testFindOneAfterUpdate() {
        val findOne = stasjonService.findOne(uuid, false)
        require(findOne is Either.Right)
        assertEquals(uuid, findOne.b.id)
        assertEquals(updateNavn, findOne.b.navn)
        assertEquals(updateType, findOne.b.type)
    }

    @Test
    @Order(6)
    fun testDelete() {
        val update = stasjonService.delete(uuid)
        assert(update is Either.Right)
    }

    @Test
    @Order(7)
    fun testFindOneFails() {
        val findOne = stasjonService.findOne(uuid, false)
        assert(findOne is Either.Left)
    }
}