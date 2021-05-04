package testutils

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.*
import io.ktor.util.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class TestContainer {
    companion object {
        @Container
        private val container = PostgreSQLContainer<Nothing>("postgres:13.2")
        private val appConfig = HoconApplicationConfig(ConfigFactory.load())

        init {
            launchContainer()
            connect()
            migrate()
        }

        private fun launchContainer() {
            val name = "testdb"
            val user = appConfig.property("ktor.db.user").getString()
            val password = appConfig.property("ktor.db.password").getString()

            container.apply {
                withDatabaseName(name)
                withUsername(user)
                withPassword(password)
                start()
            }
        }

        private fun connect() {
            val db = HikariDataSource()
            db.jdbcUrl = container.jdbcUrl
            db.username = container.username
            db.password = container.password
            Database.connect(db)
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
}