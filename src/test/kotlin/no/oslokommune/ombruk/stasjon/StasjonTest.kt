package no.oslokommune.ombruk.stasjon

import arrow.core.Either
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.stasjon.form.StasjonPostForm
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.shared.database.initDB
import org.junit.jupiter.api.*
import no.oslokommune.ombruk.testDelete
import no.oslokommune.ombruk.testGet
import no.oslokommune.ombruk.testPatch
import no.oslokommune.ombruk.testPost
import java.time.DayOfWeek
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StasjonTest {

    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    lateinit var stasjoner: List<Stasjon>

    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        stasjoner = createTestStasjoner()
    }

    @AfterEach
    fun teardown() {
        StasjonRepository.deleteAllStasjoner()
    }

    private fun createTestStasjoner() = (0..9).map {
        val s = StasjonRepository.insertStasjon(
            StasjonPostForm(
                "no.oslokommune.ombruk.stasjon.StasjonTest Stasjon$it",
                mapOf(DayOfWeek.MONDAY to listOf(LocalTime.MIDNIGHT, LocalTime.NOON))
            )
        )
        require(s is Either.Right)
        return@map s.b
    }

    @Nested
    inner class Get {
        @Test
        fun `get stasjon by id`() {
            testGet("/stasjoner/${stasjoner[3].id}") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Stasjon.serializer(), stasjoner[3]), response.content)
            }
        }

        @Test
        fun `get all stasjoner`() {
            testGet("/stasjoner") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Stasjon.serializer().list, stasjoner), response.content)
            }
        }

        @Test
        fun `get stasjon by name`() {
            testGet("/stasjoner?name=${stasjoner[6].name}") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Stasjon.serializer().list, listOf(stasjoner[6])), response.content)
            }
        }
    }

    @Nested
    inner class Post {

        @Test
        fun `create stasjon with name`() {
            val name = "MyStasjon"

            val body = """{"name": "$name"}"""

            testPost("/stasjoner", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responseStasjon = json.parse(Stasjon.serializer(), response.content!!)
                val insertedStasjon = StasjonRepository.getStasjonById(responseStasjon.id)
                require(insertedStasjon is Either.Right)
                assertEquals(responseStasjon, insertedStasjon.b)
                assertEquals(name, insertedStasjon.b.name)
            }
        }

        @Test
        fun `create stasjon with hours`() {
            val name = "MyStasjon"
            val hours = mapOf(DayOfWeek.TUESDAY to listOf(LocalTime.MIDNIGHT, LocalTime.NOON))
            val body =
                """{
                    "name": "$name",
                    "hours": {"TUESDAY": ["00:00:00Z", "12:00:00Z"]}
                }"""

            testPost("/stasjoner", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responseStasjon = json.parse(Stasjon.serializer(), response.content!!)
                val insertedStasjon = StasjonRepository.getStasjonById(responseStasjon.id)
                require(insertedStasjon is Either.Right)
                assertEquals(responseStasjon, insertedStasjon.b)
                assertEquals(name, insertedStasjon.b.name)
                assertEquals(hours, insertedStasjon.b.hours)
            }
        }

    }

    @Nested
    inner class Patch {
        @Test
        fun `update stasjon hours`() {
            val stasjonToUpdate = stasjoner[9]
            val expectedResponse = stasjonToUpdate.copy(hours = mapOf(DayOfWeek.TUESDAY to listOf(LocalTime.MIDNIGHT, LocalTime.NOON)))
            val body =
                """{
                    "id": "${stasjonToUpdate.id}",
                    "hours": {"TUESDAY": ["00:00:00Z", "12:00:00Z"]}
                }"""

            testPatch("/stasjoner", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Stasjon.serializer(), expectedResponse), response.content)

                val stasjonInRepository = StasjonRepository.getStasjonById(expectedResponse.id)
                require(stasjonInRepository is Either.Right)
                assertEquals(expectedResponse, stasjonInRepository.b)

            }
        }

        @Test
        fun `update stasjon name`() {
            val stasjonToUpdate = stasjoner[9]
            val expectedResponse = stasjonToUpdate.copy(name = "testing")
            val body =
                """{
                    "id": "${stasjonToUpdate.id}",
                    "name": "testing"
                }"""

            testPatch("/stasjoner", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Stasjon.serializer(), expectedResponse), response.content)

                val stasjonInRepository = StasjonRepository.getStasjonById(expectedResponse.id)
                require(stasjonInRepository is Either.Right)
                assertEquals(expectedResponse, stasjonInRepository.b)

            }
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `delete stasjon by id`() {
            testDelete("/stasjoner/${stasjoner[3].id}") {
                val respondedUttak = json.parse(Stasjon.serializer(), response.content!!)
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(stasjoner[3], respondedUttak)
                assertFalse(UttakRepository.exists(stasjoner[3].id))
            }
        }
    }
}