package aktor.kontakt.repository

import arrow.core.Either
import arrow.core.flatMap
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.model.KontaktCreateParams
import ombruk.backend.aktor.infrastructure.repository.KontaktRepository
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import javax.swing.text.html.parser.Entity
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class KontaktRepositoryTest {
    private lateinit var testContainer: TestContainer
    private lateinit var kontaktRepository: KontaktRepository

    @BeforeEach
    fun setup() {
        testContainer = TestContainer()
        kontaktRepository = KontaktRepository()
    }

    @Test
    fun testInsert() {
        val kontakt = object: KontaktCreateParams() {
            override val navn: String = "Oslo"
            override val telefon: String = "40404040"
            override val rolle: String = "Leder"
        }

        transaction {
            val insert = kontaktRepository.insert(kontakt)
            assert(insert is Either.Right<Kontakt>)
        }
    }

    @Test
    fun testFindOne() {
        val id = UUID.randomUUID()

        val findOne = transaction { kontaktRepository.findOne(id) }
        require(findOne is Either.Left)

        assert(findOne.a is RepositoryError.NoRowsFound)

    }
}