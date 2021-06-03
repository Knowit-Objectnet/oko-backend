package kategori.repository

import arrow.core.Either
import ombruk.backend.kategori.application.api.dto.KategoriFindDto
import ombruk.backend.kategori.domain.params.KategoriCreateParams
import ombruk.backend.kategori.infrastructure.repository.KategoriRepository
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class KategoriRepositoryTest {
    private lateinit var kategoriRepository: KategoriRepository
    private val testContainer: TestContainer = TestContainer()

    @BeforeEach
    fun setup() {
        testContainer.start()
        kategoriRepository = KategoriRepository()
    }

    @AfterEach
    fun tearDown() {
        testContainer.stop()
    }

    @Test
    fun testFindOne() {
        val id = UUID.randomUUID()
        val findOne = transaction { kategoriRepository.findOne(id) }

        require(findOne is Either.Left)
        assert(findOne.a is RepositoryError.NoRowsFound)
    }

    @Test
    fun testFind() {
        val dto = KategoriFindDto()

        val find = transaction { kategoriRepository.find(dto) }
        println(find)
        require(find is Either.Right<*>)
    }

    @Test
    fun testInsert() {

        val params = object : KategoriCreateParams() {
            override val navn: String = "Test"
        }

        val insert = transaction{ kategoriRepository.insert(params) }
        println(insert)
        require(insert is Either.Right)
        assert(insert.b.navn == params.navn)
        //assert(insert.b.id == params.type)

    }
}