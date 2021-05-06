package aktor.stasjon.repository

import arrow.core.Either
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.shared.error.RepositoryError
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

        val findOne = stasjonRepository.findOne(id)
        require(findOne is Either.Left)

        assert(findOne.a is RepositoryError.NoRowsFound)

    }
}