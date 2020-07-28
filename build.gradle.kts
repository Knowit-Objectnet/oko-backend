

val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val exposed_version: String by project
val valiktor_version: String by project

group = "oslokommune.ombruk"

plugins {
    application
    kotlin("jvm") version "1.3.70"
    kotlin("plugin.serialization") version "1.3.61"
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("org.flywaydb.flyway") version "6.4.4"
    jacoco
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenLocal()
    jcenter()
}

dependencies {

    fun ktor(module: String) = "io.ktor:ktor-$module:$ktor_version"
    fun exposed(module: String) = "org.jetbrains.exposed:exposed-$module:$exposed_version"
    fun valiktor(module: String) = "org.valiktor:valiktor-$module:$valiktor_version"
    implementation(kotlin("stdlib-jdk8"))
    implementation(ktor("server-netty"))
    implementation(ktor("server-core"))
    implementation(ktor("server-host-common"))
    implementation(ktor("client-apache"))
    implementation(ktor("serialization"))
    implementation(ktor("auth-jwt"))
    implementation(exposed("core"))
    implementation(exposed("dao"))
    implementation(exposed("jdbc"))
    implementation(exposed("java-time"))

    implementation(valiktor("core"))
    implementation(valiktor("javatime"))
    implementation("io.arrow-kt:arrow-core:0.10.5")
    implementation("org.postgresql:postgresql:42.2.2")
    implementation("com.zaxxer:HikariCP:3.4.5")
    implementation("org.flywaydb:flyway-core:6.4.4")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.h2database:h2:1.4.200")

    testImplementation(ktor("server-tests"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClassName
            )
        )
    }
}

flyway {
    url = System.getenv("CALENDAR_JDBC_URL")
    user = System.getenv("CALENDAR_DB_USER")
    password = System.getenv("CALENDAR_DB_PASSWORD")
    baselineOnMigrate = true
    locations = arrayOf("filesystem:resources/db/migration")
}
