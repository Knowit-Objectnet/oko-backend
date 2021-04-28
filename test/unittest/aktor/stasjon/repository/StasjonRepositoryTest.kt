package aktor.stasjon.repository

import arrow.core.Either
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.*
import io.ktor.util.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import ombruk.backend.aktor.application.service.StasjonService
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.reporting.database.ReportRepository
import ombruk.backend.shared.error.RepositoryError
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.junit.Assert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import kotlin.math.exp
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class StasjonRepositoryTest {
    /*init {
        runBlocking {
            TestContainer()
        }
    }*/


    private lateinit var stasjonRepository: StasjonRepository

    @BeforeEach
    fun setup() {
        stasjonRepository = StasjonRepository()
        TestContainer()
    }

    @Test
    fun testFindOne() {
        val id = 1

        val findOne = stasjonRepository.findOne(id)
        require(findOne is Either.Left)

        assert(findOne.a is RepositoryError.NoRowsFound)

    }
}