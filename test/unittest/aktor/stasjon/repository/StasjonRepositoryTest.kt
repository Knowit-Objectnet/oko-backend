package aktor.stasjon.repository

import arrow.core.Either
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.domain.model.StasjonCreateParams
import ombruk.backend.aktor.domain.model.StasjonFindParams
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class StasjonRepositoryTest {
    private lateinit var stasjonRepository: StasjonRepository
    private val testContainer: TestContainer = TestContainer()

    @BeforeEach
    fun setup() {
        testContainer.start()
        stasjonRepository = StasjonRepository()
    }

    @AfterEach
    fun tearDown() {
        testContainer.stop()
    }

    @Test
    fun testFindOne() {
        val id = UUID.randomUUID()
        val findOne = transaction { stasjonRepository.findOne(id) }

        require(findOne is Either.Left)
        assert(findOne.a is RepositoryError.NoRowsFound)
    }

    @Test
    fun testFind() {
        val findParams = object : StasjonFindParams() {
            override val id: UUID? = null
            override val navn: String? = null
            override val type: StasjonType? = null
        }

        val find = transaction { stasjonRepository.find(findParams) }
        println(find)
        assert(find is Either.Right)
    }

    @Test
    fun testInsert() {

        val params = object : StasjonCreateParams() {
            override val navn: String = "TestStasjon"
            override val type: StasjonType = StasjonType.GJENBRUK
        }

        val insert = transaction{ stasjonRepository.insert(params) }
        println(insert)
        require(insert is Either.Right)
        assert(insert.b.navn == params.navn)
        assert(insert.b.type == params.type)

        val errorInsert = transaction { stasjonRepository.insert(params) }
        println(errorInsert)
        assert(errorInsert.isLeft())

    }
}