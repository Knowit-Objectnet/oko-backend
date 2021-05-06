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

    @BeforeEach
    fun setup() {
        stasjonRepository = StasjonRepository()
        TestContainer()
    }

    @Test
    fun testFindOne() {
        val id = UUID.randomUUID()

        val findOne = transaction { stasjonRepository.findOne(id) }

        println(findOne)

        require(findOne is Either.Left)

        assert(findOne.a is RepositoryError.NoRowsFound)

    }
}