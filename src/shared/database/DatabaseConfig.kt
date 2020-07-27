package ombruk.backend.shared.database

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.HoconApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

@KtorExperimentalAPI
fun initDB() {
    val appConfig = HoconApplicationConfig(ConfigFactory.load())
    val hikariConfig = HikariConfig()
    hikariConfig.jdbcUrl = appConfig.property("ktor.db.jdbcUrl").getString()
    hikariConfig.username = appConfig.property("ktor.db.user").getString()
    hikariConfig.password = appConfig.property("ktor.db.password").getString()
    hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
    hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

    Database.connect(HikariDataSource(hikariConfig))

    migrate()
}

@KtorExperimentalAPI
fun migrate() {
    val appConfig = HoconApplicationConfig(ConfigFactory.load())

    val flyway = Flyway.configure().dataSource(
        appConfig.property("ktor.db.jdbcUrl").getString(),
        appConfig.property("ktor.db.user").getString(),
        appConfig.property("ktor.db.password").getString()
    ).locations(
        appConfig.property("ktor.db.migrationsLocation").getString()
    ).baselineOnMigrate(true)
    .load()

    flyway.migrate()
}
