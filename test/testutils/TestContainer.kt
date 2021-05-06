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

    fun <T:Any> String.execAndMap(transform : (ResultSet) -> T) : List<T> {
        val result = arrayListOf<T>()
        TransactionManager.current().exec(this) { rs ->
            while (rs.next()) {
                result += transform(rs)
            }
        }
        return result
    }

    fun ex(query: String) {
        query.execAndMap { rs ->
            rs.getString("u.name") to rs.getString("c.name")
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
        /*var result = arrayListOf()

        runBlocking {
            transaction(database) {
                exec(query) { rs ->
                    while(rs.next()) {
                        result += rs.row
                    }
                }
                println("hererereer")
                println(result)
            }
        }
        return result*/

        /*val name = "testdb"
        val user = appConfig.property("ktor.db.user").getString()
        val password = appConfig.property("ktor.db.password").getString()
        return db.connection.nativeSQL("SELECT VERSION();")*/
        //return TransactionManager.current().exec("SELECT VERSION();") { it.next(); it.getString(1) }
        /*transaction {
            val conn = TransactionManager.current().connection
            val statement = conn.
            val query = "REFRESH MATERIALIZED VIEW someview"
            statement.execute(query)
        }*/
    }

    /*fun exec(query: String) : String? {
        val queries = listOf("test")
        val hikar = HikariDataSource().connection.nativeSQL(query)
        //val test = connection.executeInBatch(queries)
        println("HERE:")
        println(hikar)
        return hikar
    }*/


    /*fun Transaction.exec(sql: String, body: PreparedStatement.() -> Unit) : ResultSet? {
        val queries = listOf("test")
        val test = connection.executeInBatch(queries)
        println("HERE:")
        println(test)
        return connection.prepareStatement(sql).apply(body).run {
            if (sql.toLowerCase().startsWith("select "))
                executeQuery()
            else {
                executeUpdate()
                resultSet
            }
        }
    }*/
}