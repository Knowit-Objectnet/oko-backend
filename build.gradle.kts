val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val exposed_version: String by project
val valiktor_version: String by project

group = "no.oslokommune.ombruk"

plugins {
    application
    kotlin("jvm") version "1.3.70"
    kotlin("plugin.serialization") version "1.3.61"
    id("com.github.johnrengelman.shadow") version "5.0.0"
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
    implementation(ktor("locations"))
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

    testImplementation("io.mockk:mockk:1.10.0")
    testImplementation(kotlin("test-junit5"))
    testImplementation(ktor("server-tests"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
}

tasks.test {
    useJUnitPlatform()
}
tasks.withType<Jar> {
    manifest {
        attributes(mapOf("Main-Class" to application.mainClassName))
    }
}
/*
kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test/unittest", "test/testutils")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")


sourceSets {
    create("integrationTest") {
        withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
            kotlin.srcDirs("test/integrationtest", "test/testutils")
            resources.srcDir("testresources")
            compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
            runtimeClasspath += output + compileClasspath + sourceSets["test"].runtimeClasspath
        }
    }
}



task<Test>("integrationTest") {
    description = "Runs the integration tests"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    mustRunAfter(tasks["test"])
    useJUnitPlatform()
}
*/