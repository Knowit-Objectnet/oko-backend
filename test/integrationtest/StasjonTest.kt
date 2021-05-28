import arrow.core.Either
import arrow.core.right
import io.ktor.util.*
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.application.api.dto.StasjonInsertDto
import ombruk.backend.aktor.application.api.dto.StasjonFindDto
import ombruk.backend.aktor.application.api.dto.StasjonUpdateDto
import ombruk.backend.aktor.application.service.StasjonService
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
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
import kotlin.test.assertNotEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(MockKExtension::class)
@Testcontainers
class StasjonTest {
    private val testContainer: TestContainer = TestContainer()
    private lateinit var stasjonService: StasjonService
    private var stasjonRepository = StasjonRepository()
    private var keycloakGroupIntegration = mockkClass(KeycloakGroupIntegration::class)

    @OptIn(KtorExperimentalAPI::class)
    @BeforeAll
    fun setup() {
        testContainer.start()
        stasjonService = StasjonService(stasjonRepository, keycloakGroupIntegration)
    }

    private lateinit var navn: String
    private lateinit var type: StasjonType
    private lateinit var uuid: UUID
    private lateinit var updateNavn: String
    private lateinit var updateType: StasjonType

    @Test
    @Order(1)
    fun testInsert(@MockK expected: Any) {
        navn = "Grefsen"
        type = StasjonType.GJENBRUK
        every { keycloakGroupIntegration.createGroup(navn, any<UUID>()) } returns expected.right()

        val stasjon = StasjonInsertDto(navn, type)
        val save = stasjonService.save(stasjon)
        assert(save is Either.Right<Stasjon>)
    }

    @Test
    @Order(2)
    fun testFind() {
        val stasjon = StasjonFindDto(navn)
        val find = stasjonService.find(stasjon)
        require(find is Either.Right)
        assertTrue(find.b.count() == 1)
        assertEquals(navn, find.b[0].navn)
        assertEquals(type, find.b[0].type)
        // UUID for next test
        uuid = find.b[0].id
    }

    @Test
    @Order(3)
    fun testFindOne() {
        val findOne = stasjonService.findOne(uuid)
        require(findOne is Either.Right)
        assertEquals(uuid, findOne.b.id)
        assertEquals(navn, findOne.b.navn)
        assertEquals(type, findOne.b.type)
    }

    @OptIn(KtorExperimentalAPI::class)
    @Test
    @Order(4)
    fun testUpdate(@MockK expected: Unit) {
        updateNavn = "Grefsen Mini"
        updateType = StasjonType.MINI
        every { keycloakGroupIntegration.updateGroup(navn, updateNavn) } returns expected.right()

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
        val findOne = stasjonService.findOne(uuid)
        require(findOne is Either.Right)
        assertEquals(uuid, findOne.b.id)
        assertEquals(updateNavn, findOne.b.navn)
        assertEquals(updateType, findOne.b.type)
    }

    @Test
    @Order(6)
    fun testDelete(@MockK expected: Any) {
        every { keycloakGroupIntegration.deleteGroup(updateNavn) } returns expected.right()

        val update = stasjonService.delete(uuid)
        assert(update is Either.Right)
    }

    @Test
    @Order(7)
    fun testFindOneFails() {
        val findOne = stasjonService.findOne(uuid)
        assert(findOne is Either.Left)
    }
}