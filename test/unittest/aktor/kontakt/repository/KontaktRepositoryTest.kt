package aktor.kontakt.repository

import arrow.core.Either
import arrow.core.right
import arrow.core.rightIfNotNull
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.model.KontaktCreateParams
import ombruk.backend.aktor.domain.model.KontaktFindParams
import ombruk.backend.aktor.infrastructure.repository.KontaktRepository
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class KontaktRepositoryTest {
    private val testContainer: TestContainer = TestContainer()
    private lateinit var kontaktRepository: KontaktRepository

    @BeforeEach
    fun setup() {
        testContainer.start()
        kontaktRepository = KontaktRepository()
    }

    @AfterEach
    fun tearDown() {
        testContainer.stop()
    }

    @Test
    fun testInsert() {
        val aktorId = UUID.randomUUID()
        val kontakt = object: KontaktCreateParams() {
            override val aktorId: UUID = aktorId
            override val navn: String = "Oslo"
            override val telefon: String = "40404040"
            override val epost: String = "leder@oslo.no"
            override val rolle: String = "Leder"
        }

        transaction {
            val insert = kontaktRepository.insert(kontakt)
            assert(insert is Either.Right)
            val k = insert.fold({ null }, {it})
            if (k != null) {
                assertEquals(k.aktorId, aktorId)
            }
        }
    }

    @Test
    fun testFindOne() {
        val id = UUID.randomUUID()
        val findOne = transaction { kontaktRepository.findOne(id) }
        require(findOne is Either.Left)
        assert(findOne.a is RepositoryError.NoRowsFound)

    }

    @Test
    fun testFind() {
        val findParams = object : KontaktFindParams() {
            override val id: UUID? = null
            override val aktorId: UUID? = null
            override val navn: String? = null
            override val telefon: String? = null
            override val epost: String? = null
            override val rolle: String? = null
        }

        val find = transaction { kontaktRepository.find(findParams) }
        println(find)
        require(find is Either.Right)
        assert(find.b.isEmpty())
    }
}