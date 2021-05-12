package testutils

import arrow.core.extensions.id.applicative.unit
import arrow.core.left
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.PreparedStatement
import java.sql.ResultSet

@Testcontainers
class TestContainer {
    companion object {
        @Container
        private val container = PostgreSQLContainer<Nothing>("postgres:13.2")
        private val appConfig = HoconApplicationConfig(ConfigFactory.load())
        private lateinit var database : Database

        init {
            launchContainer()
        }

        private fun launchContainer() {
            val name = "testdb"
            val user = appConfig.property("ktor.db.user").getString()
            val password = appConfig.property("ktor.db.password").getString()

            container.apply {
                withDatabaseName(name)
                withUsername(user)
                withPassword(password)
            }
        }

        private fun connect() {
            val db = HikariDataSource()
            db.jdbcUrl = container.jdbcUrl
            db.username = container.username
            db.password = container.password
            database = Database.connect(db)
        }

        @KtorExperimentalAPI
        private fun migrate() {
            val flyway = Flyway.configure().dataSource(
                container.jdbcUrl,
                container.username,
                container.password
            ).locations(
                appConfig.property("ktor.db.migrationsLocation").getString()
            ).baselineOnMigrate(true)
                .load()

            flyway.migrate()
        }
    }

    fun <T:Any> exec(query: String, transform : (ResultSet) -> T) : List<T> {
        val result = arrayListOf<T>()

        transaction(database) {
            exec(query) { rs ->
                while (rs.next()) {
                    result += transform(rs)
                }
            }
        }

        return result
    }

    fun start() {
        container.start()
        connect()
        migrate()
    }
    fun stop() { container.stop() }
}