package aktor.stasjon.repository

import arrow.core.Either
import arrow.core.extensions.either.foldable.isEmpty
import arrow.core.extensions.either.foldable.size
import arrow.core.right
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.domain.model.StasjonCreateParams
import ombruk.backend.aktor.domain.model.StasjonFindParams
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class StasjonRepositoryTest {
    private lateinit var stasjonRepository: StasjonRepository
    private lateinit var testContainer: TestContainer

    @BeforeEach
    fun setup() {
        stasjonRepository = StasjonRepository()
        testContainer = TestContainer()
    }

    /*@AfterEach
    fun tearDown() {
        testContainer.disconnect()
    }*/

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
        require(find is Either.Right)
        assert(find.b.isEmpty())
    }

    @Test
    fun testInsert() {

        val params = object : StasjonCreateParams() {
            override val navn: String = "Grefsen"
            override val type: StasjonType = StasjonType.GJENBRUK
        }

        val insert = transaction{ stasjonRepository.insert(params) }
        println(insert)
        require(insert is Either.Right)
        assert(insert.b.navn == params.navn)
        assert(insert.b.type == params.type)

    }
}