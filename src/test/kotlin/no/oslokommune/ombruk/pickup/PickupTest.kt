package no.oslokommune.ombruk.pickup

import arrow.core.Either
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.stasjon.form.StasjonPostForm
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.stasjon.service.StasjonService
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.partner.form.PartnerPostForm
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.partner.service.PartnerService
import no.oslokommune.ombruk.pickup.database.PickupRepository
import no.oslokommune.ombruk.pickup.form.pickup.PickupPostForm
import no.oslokommune.ombruk.pickup.model.Pickup
import no.oslokommune.ombruk.shared.database.initDB
import org.junit.jupiter.api.*
import no.oslokommune.ombruk.testDelete
import no.oslokommune.ombruk.testGet
import no.oslokommune.ombruk.testPatch
import no.oslokommune.ombruk.testPost
import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PickupTest {

    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    var partners: List<Partner>
    var stasjoner: List<Stasjon>
    lateinit var pickups: List<Pickup>

    init {
        initDB()
        partners = createTestPartners()
        stasjoner = createTestStasjoner()
    }

    @BeforeEach
    fun setup() {
        pickups = createPickups()
    }

    @AfterEach
    fun teardown() {
        PickupRepository.deleteAllPickups()
    }

    @AfterAll
    fun finish() {
        PartnerRepository.deleteAllPartners()
        StasjonRepository.deleteAllStasjoner()
    }

    private fun createTestPartners() = (0..9).map {
        val p = PartnerService.savePartner(
            PartnerPostForm(
                "Partner$it",
                "Description",
                "1234567$it",
                "test$it@gmail.com"
            )
        )
        require(p is Either.Right)
        return@map p.b
    }

    private fun createTestStasjoner() = (0..5).map {
        val s = StasjonService.saveStasjon(StasjonPostForm("Stasjon$it"))
        require(s is Either.Right)
        return@map s.b
    }

    private fun createPickups(): List<Pickup> {
        var stasjonCounter = 0
        return (0..100L).map {
            val p = PickupRepository.savePickup(
                PickupPostForm(
                    LocalDateTime.parse("2020-07-06T15:48:06").plusDays(it),
                    LocalDateTime.parse("2020-07-06T16:48:06").plusDays(it),
                    "Test",
                    stasjoner[stasjonCounter].id
                )
            )

            stasjonCounter = (stasjonCounter + 1) % 6
            require(p is Either.Right)
            return@map p.b
        }
    }

    @Nested
    inner class Get {

        @Test
        fun `get pickup by id`() {
            testGet("/pickups/${pickups[45].id}") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer(), pickups[45]), response.content)
            }
        }

        @Test
        fun `get all pickups`() {
            testGet("/pickups/") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer().list, pickups), response.content)
            }
        }

        @Test
        fun `get pickups by stasjon id`() {
            testGet("/pickups?stasjonId=${stasjoner[2].id}") {
                val expected = pickups.filter { it.stasjon == stasjoner[2] }
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get pickups by startDateTime`() {
            testGet("/pickups?startDateTime=2020-08-10T15:00:00") {
                val expected = pickups.filter { it.startDateTime >= LocalDateTime.parse("2020-08-10T15:00:00") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get pickups by endDateTime`() {
            testGet("/pickups?endDateTime=2020-08-10T15:00:00") {
                val expected = pickups.filter { it.startDateTime <= LocalDateTime.parse("2020-08-10T15:00:00") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get pickups by datetime range`() {
            testGet("/pickups?startDateTime=2020-08-10T15:00:00&endDateTime=2020-09-10T15:00:00") {
                val expected = pickups.filter { it.startDateTime >= LocalDateTime.parse("2020-08-10T15:00:00") }
                    .filter { it.startDateTime <= LocalDateTime.parse("2020-09-10T15:00:00") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer().list, expected), response.content)
            }
        }
    }

    @Nested
    inner class Post {
        @Test
        fun `create pickup with description`() {
            val startDateTime = LocalDateTime.parse("2020-07-06T15:00:00")
            val endDateTime = LocalDateTime.parse("2020-07-06T16:00:00")
            val stasjonId = stasjoner[Random.nextInt(1, 5)].id
            val description = "this is a description"

            val body =
                """{
                    "startDateTime": "2020-07-06T15:00:00",
                    "endDateTime": "2020-07-06T16:00:00",
                    "stasjonId": "$stasjonId",
                    "description": "$description"
                }"""

            testPost("/pickups", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responsePickup = json.parse(Pickup.serializer(), response.content!!)
                val insertedPickup = PickupRepository.getPickupById(responsePickup.id)
                require(insertedPickup is Either.Right)
                assertEquals(responsePickup, insertedPickup.b)
                assertEquals(startDateTime, responsePickup.startDateTime)
                assertEquals(endDateTime, responsePickup.endDateTime)
                assertEquals(stasjonId, responsePickup.stasjon.id)
                assertEquals(description, responsePickup.description)
            }
        }

        @Test
        fun `create pickup without description`() {
            val startDateTime = LocalDateTime.parse("2020-07-06T15:00:00")
            val endDateTime = LocalDateTime.parse("2020-07-06T16:00:00")
            val stasjonId = stasjoner[Random.nextInt(1, 5)].id

            val body =
                """{
                    "startDateTime": "2020-07-06T15:00:00",
                    "endDateTime": "2020-07-06T16:00:00",
                    "stasjonId": "$stasjonId"
                }"""

            testPost("/pickups", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responsePickup = json.parse(Pickup.serializer(), response.content!!)
                val insertedPickup = PickupRepository.getPickupById(responsePickup.id)
                require(insertedPickup is Either.Right)
                assertEquals(responsePickup, insertedPickup.b)
                assertEquals(startDateTime, responsePickup.startDateTime)
                assertEquals(endDateTime, responsePickup.endDateTime)
                assertEquals(stasjonId, responsePickup.stasjon.id)
                assertEquals(null, responsePickup.description)
            }
        }
    }

    @Nested
    inner class Patch {

        @Test
        fun `update pickup startDateTime`() {
            val pickupToUpdate = pickups[87]
            val expectedResponse = pickupToUpdate.copy(startDateTime = pickupToUpdate.startDateTime.minusHours(1))
            val body =
                """{
                    "id": "${pickupToUpdate.id}",
                    "startDateTime": "${pickupToUpdate.startDateTime.minusHours(1)}"
                }"""

            testPatch("/pickups", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer(), expectedResponse), response.content)

                val pickupInRepository = PickupRepository.getPickupById(expectedResponse.id)
                require(pickupInRepository is Either.Right)
                assertEquals(expectedResponse, pickupInRepository.b)

            }
        }

        @Test
        fun `update pickup endDateTime`() {
            val pickupToUpdate = pickups[23]
            val expectedResponse = pickupToUpdate.copy(endDateTime = pickupToUpdate.endDateTime.plusHours(1))
            val body =
                """{
                    "id": "${pickupToUpdate.id}",
                    "endDateTime": "${pickupToUpdate.endDateTime.plusHours(1)}"
                }"""

            testPatch("/pickups", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer(), expectedResponse), response.content)

                val pickupInRepository = PickupRepository.getPickupById(expectedResponse.id)
                require(pickupInRepository is Either.Right)
                assertEquals(expectedResponse, pickupInRepository.b)

            }
        }

        @Test
        fun `update pickup startDateTime and endDateTime`() {
            val pickupToUpdate = pickups[23]
            val expectedResponse = pickupToUpdate.copy(
                startDateTime = pickupToUpdate.startDateTime.minusHours(1),
                endDateTime = pickupToUpdate.endDateTime.plusHours(1)
            )
            val body =
                """{
                    "id": "${pickupToUpdate.id}",
                    "startDateTime": "${pickupToUpdate.startDateTime.minusHours(1)}",
                    "endDateTime": "${pickupToUpdate.endDateTime.plusHours(1)}"
                }"""

            testPatch("/pickups", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer(), expectedResponse), response.content)

                val pickupInRepository = PickupRepository.getPickupById(expectedResponse.id)
                require(pickupInRepository is Either.Right)
                assertEquals(expectedResponse, pickupInRepository.b)

            }
        }

        @Test
        fun `update pickup description`() {
            val pickupToUpdate = pickups[87]
            val expectedResponse = pickupToUpdate.copy(description = "This is a test")
            val body =
                """{
                    "id": "${pickupToUpdate.id}",
                    "description": "This is a test"
                }"""

            testPatch("/pickups", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer(), expectedResponse), response.content)

                val pickupInRepository = PickupRepository.getPickupById(expectedResponse.id)
                require(pickupInRepository is Either.Right)
                assertEquals(expectedResponse, pickupInRepository.b)

            }
        }

        @Test
        fun `update pickup chosen partner`() {
            val pickupToUpdate = pickups[87]
            val expectedResponse = pickupToUpdate.copy(chosenPartner = partners[6])
            val body =
                """{
                    "id": "${pickupToUpdate.id}",
                    "chosenPartnerId": "${partners[6].id}"
                }"""

            testPatch("/pickups", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer(), expectedResponse), response.content)

                val pickupInRepository = PickupRepository.getPickupById(expectedResponse.id)
                require(pickupInRepository is Either.Right)
                assertEquals(expectedResponse, pickupInRepository.b)

            }
        }
    }

    @Nested
    inner class Delete {

        fun `delete pickup by id`() {
            testDelete("/pickups/92") {
                val respondedUttak = json.parse(Pickup.serializer().list, response.content!!)
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(listOf(pickups[92]), respondedUttak)
                assertFalse(PickupRepository.exists(pickups[92].id))
            }
        }
    }
}