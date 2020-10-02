package no.oslokommune.ombruk.station

import arrow.core.Either
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.station.database.StationRepository
import no.oslokommune.ombruk.station.form.StationPostForm
import no.oslokommune.ombruk.station.model.Station
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

@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StationTest {

    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    lateinit var stations: List<Station>

    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        stations = createTestStations()
    }

    @AfterEach
    fun teardown() {
        StationRepository.deleteAllStations()
    }

    private fun createTestStations() = (0..9).map {
        val s = StationRepository.insertStation(
            StationPostForm(
                "no.oslokommune.ombruk.station.StationTest Station$it",
                mapOf(DayOfWeek.MONDAY to listOf(LocalTime.MIDNIGHT, LocalTime.NOON))
            )
        )
        require(s is Either.Right)
        return@map s.b
    }

    @Nested
    inner class Get {
        @Test
        fun `get station by id`() {
            testGet("/stations/${stations[3].id}") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Station.serializer(), stations[3]), response.content)
            }
        }

        @Test
        fun `get all stations`() {
            testGet("/stations") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Station.serializer().list, stations), response.content)
            }
        }

        @Test
        fun `get station by name`() {
            testGet("/stations?name=${stations[6].name}") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Station.serializer().list, listOf(stations[6])), response.content)
            }
        }
    }

    @Nested
    inner class Post {

        @Test
        fun `create station with name`() {
            val name = "MyStation"

            val body = """{"name": "$name"}"""

            testPost("/stations", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responseStation = json.parse(Station.serializer(), response.content!!)
                val insertedStation = StationRepository.getStationById(responseStation.id)
                require(insertedStation is Either.Right)
                assertEquals(responseStation, insertedStation.b)
                assertEquals(name, insertedStation.b.name)
            }
        }

        @Test
        fun `create station with hours`() {
            val name = "MyStation"
            val hours = mapOf(DayOfWeek.TUESDAY to listOf(LocalTime.MIDNIGHT, LocalTime.NOON))
            val body =
                """{
                    "name": "$name",
                    "hours": {"TUESDAY": ["00:00:00Z", "12:00:00Z"]}
                }"""

            testPost("/stations", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responseStation = json.parse(Station.serializer(), response.content!!)
                val insertedStation = StationRepository.getStationById(responseStation.id)
                require(insertedStation is Either.Right)
                assertEquals(responseStation, insertedStation.b)
                assertEquals(name, insertedStation.b.name)
                assertEquals(hours, insertedStation.b.hours)
            }
        }

    }

    @Nested
    inner class Patch {
        @Test
        fun `update station hours`() {
            val stationToUpdate = stations[9]
            val expectedResponse = stationToUpdate.copy(hours = mapOf(DayOfWeek.TUESDAY to listOf(LocalTime.MIDNIGHT, LocalTime.NOON)))
            val body =
                """{
                    "id": "${stationToUpdate.id}",
                    "hours": {"TUESDAY": ["00:00:00Z", "12:00:00Z"]}
                }"""

            testPatch("/stations", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Station.serializer(), expectedResponse), response.content)

                val stationInRepository = StationRepository.getStationById(expectedResponse.id)
                require(stationInRepository is Either.Right)
                assertEquals(expectedResponse, stationInRepository.b)

            }
        }

        @Test
        fun `update station name`() {
            val stationToUpdate = stations[9]
            val expectedResponse = stationToUpdate.copy(name = "testing")
            val body =
                """{
                    "id": "${stationToUpdate.id}",
                    "name": "testing"
                }"""

            testPatch("/stations", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Station.serializer(), expectedResponse), response.content)

                val stationInRepository = StationRepository.getStationById(expectedResponse.id)
                require(stationInRepository is Either.Right)
                assertEquals(expectedResponse, stationInRepository.b)

            }
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `delete station by id`() {
            testDelete("/stations/${stations[3].id}") {
                val respondedUttaks = json.parse(Station.serializer(), response.content!!)
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(stations[3], respondedUttaks)
                assertFalse(UttakRepository.exists(stations[3].id))
            }
        }
    }
}