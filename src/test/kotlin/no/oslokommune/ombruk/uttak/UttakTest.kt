package no.oslokommune.ombruk.uttak

import arrow.core.Either
import arrow.core.extensions.list.functorFilter.filter
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.uttak.form.UttakDeleteForm
import no.oslokommune.ombruk.uttak.form.UttakGetForm
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.stasjon.form.StasjonPostForm
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.uttak.service.UttakService
import no.oslokommune.ombruk.stasjon.service.StasjonService
import no.oslokommune.ombruk.partner.database.SamPartnerRepository
import no.oslokommune.ombruk.partner.form.PartnerPostForm
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.partner.service.PartnerService
import no.oslokommune.ombruk.shared.database.initDB
import org.junit.jupiter.api.*
import no.oslokommune.ombruk.testDelete
import no.oslokommune.ombruk.testGet
import no.oslokommune.ombruk.testPatch
import no.oslokommune.ombruk.testPost
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UttakTest {

    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    var partnere: List<Partner>
    var stasjoner: List<Stasjon>
    lateinit var uttak: List<Uttak>

    init {
        initDB()
        partnere = createTestPartnere()
        stasjoner = createTestStasjoner()
    }

    @BeforeEach
    fun setup() {
        uttak = createTestUttak()
    }

    @AfterEach
    fun teardown() {
        UttakRepository.deleteUttak(UttakDeleteForm())
    }

    @AfterAll
    fun finish() {
        SamPartnerRepository.deleteAllPartnere()
        StasjonRepository.deleteAllStasjoner()
    }

    private fun createTestPartnere() = (0..9).map {
        val p = PartnerService.savePartner(
            PartnerPostForm(
                "TestPartner$it",
                "Description",
                "1234567$it",
                "test$it@gmail.com"
            )
        )
        require(p is Either.Right)
        return@map p.b
    }

    private fun createTestStasjoner() = (0..5).map {
        val s = StasjonService.saveStasjon(StasjonPostForm("no.oslokommune.ombruk.uttak.UttakTest Stasjon$it", hours = openHours()))
        require(s is Either.Right)
        return@map s.b
    }

    private fun createTestUttak(): List<Uttak> {
        var partnerCounter = 0
        var stasjonCounter = 0
        return (1..100L).map {
            val e = UttakService.saveUttak(
                UttakPostForm(
                    LocalDateTime.parse("2020-07-06T15:48:06").plusDays(it),
                    LocalDateTime.parse("2020-07-06T16:48:06").plusDays(it),
                    stasjoner[stasjonCounter].id,
                    partnere[partnerCounter].id
                )
            )

            partnerCounter++
            if (partnerCounter % 10 == 0) {
                partnerCounter = 0
                stasjonCounter = (stasjonCounter + 1) % 6
            }

            require(e is Either.Right)
            return@map e.b
        }
    }

    @Nested
    inner class Get {
        @Test
        fun `get uttak by id`() {
            testGet("/uttak/${uttak[45].id}") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer(), uttak[45]), response.content)
            }
        }

        @Test
        fun `get all uttak`() {
            testGet("/uttak/") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer().list, uttak), response.content)
            }
        }

        @Test
        fun `get uttak by stasjonId`() {
            testGet("/uttak?stasjonId=${stasjoner[3].id}") {
                val expected = uttak.filter { it.stasjon == stasjoner[3] }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get uttak by partnerId`() {
            testGet("/uttak?partnerId=${partnere[7].id}") {
                val expected = uttak.filter { it.partner == partnere[7] }
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get uttak by stasjonId and partnerId`() {
            testGet("/uttak?stasjonId=${stasjoner[2].id}&partnerId=${partnere[4].id}") {
                val expected = uttak.filter { it.stasjon == stasjoner[2] }.filter { it.partner == partnere[4] }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get uttak from date`() {
            testGet("/uttak?fromDate=2020-08-10T15:00:00") {
                val expected = uttak.filter { it.startDateTime >= LocalDateTime.parse("2020-08-10T15:00:00") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get uttak to date`() {
            testGet("/uttak?toDate=2020-08-10T15:00:00") {
                val expected = uttak.filter { it.startDateTime <= LocalDateTime.parse("2020-08-10T15:00:00") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get uttak in date range`() {
            testGet("/uttak?fromDate=2020-08-10T15:00:00&toDate=2020-09-10T15:00:00") {
                val expected = uttak.filter { it.startDateTime >= LocalDateTime.parse("2020-08-10T15:00:00") }
                    .filter { it.startDateTime <= LocalDateTime.parse("2020-09-10T15:00:00") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer().list, expected), response.content)
            }
        }

    }

    @Nested
    inner class Post {

        @Test
        fun `create single uttak`() {
            val startDateTime = LocalDateTime.parse("2020-07-06T15:00:00")
            val endDateTime = LocalDateTime.parse("2020-07-06T16:00:00")
            val stasjonId = stasjoner[Random.nextInt(1, 5)].id
            val partnerId = partnere[Random.nextInt(1, 9)].id

            val body =
                """{
                    "startDateTime": "2020-07-06T15:00:00",
                    "endDateTime": "2020-07-06T16:00:00",
                    "stasjonId": "$stasjonId",
                    "partnerId": "$partnerId"
                }"""

            testPost("/uttak", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responseUttak = json.parse(Uttak.serializer(), response.content!!)
                val insertedUttak = UttakRepository.getUttakByID(responseUttak.id)
                require(insertedUttak is Either.Right)
                assertEquals(responseUttak, insertedUttak.b)
                assertEquals(startDateTime, responseUttak.startDateTime)
                assertEquals(endDateTime, responseUttak.endDateTime)
                assertEquals(stasjonId, responseUttak.stasjon.id)
                assertEquals(partnerId, responseUttak.partner?.id)
            }
        }

        @Test
        fun `create recurring uttak`() {
            val startDateTime = LocalDateTime.parse("2020-07-06T15:00:00")
            val endDateTime = LocalDateTime.parse("2020-07-06T16:00:00")
            val stasjonId = stasjoner[Random.nextInt(1, 5)].id
            val partnerId = partnere[Random.nextInt(1, 9)].id
            val rRule = GjentakelsesRegel(count = 5)

            val body =
                """{
                    "startDateTime": "2020-07-06T15:00:00",
                    "endDateTime": "2020-07-06T16:00:00",
                    "stasjonId": "$stasjonId",
                    "partnerId": "$partnerId",
                    "gjentakelsesRegel": { "count": "${rRule.count}" }
                }"""

            testPost("/uttak", body) {
                val responseUttak = json.parse(Uttak.serializer(), response.content!!)
                val insertedUttak =
                    UttakRepository.getUttak(UttakGetForm(gjentakelsesRegelID = responseUttak.gjentakelsesRegel!!.id))

                assertEquals(HttpStatusCode.OK, response.status())
                require(insertedUttak is Either.Right)
                assertTrue { insertedUttak.b.contains(responseUttak) }
                assertEquals(insertedUttak.b.size, 5)

                insertedUttak.b.forEachIndexed { index, uttak ->
                    assertEquals(startDateTime.plusDays(7L * index), uttak.startDateTime)
                    assertEquals(endDateTime.plusDays(7L * index), uttak.endDateTime)
                    assertEquals(stasjonId, uttak.stasjon.id)
                    assertEquals(partnerId, uttak.partner?.id)
                }
            }
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `delete uttak by id`() {
            testDelete("/uttak?uttakId=${uttak[68].id}") {
                val respondedUttak = json.parse(Uttak.serializer().list, response.content!!)
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(listOf(uttak[68]), respondedUttak)
                assertFalse(UttakRepository.exists(uttak[68].id))
            }
        }

        @Test
        fun `delete uttak by stasjon id`() {
            testDelete("/uttak?stasjonId=${stasjoner[1].id}") {
                val respondedUttak = json.parse(Uttak.serializer().list, response.content!!)
                val deletedUttak = uttak.filter { it.stasjon.id == stasjoner[1].id }
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(deletedUttak, respondedUttak)
                deletedUttak.forEach {
                    assertFalse(UttakRepository.exists(it.id))
                }
            }
        }

        @Test
        fun `delete uttak by partner id`() {
            testDelete("/uttak?partnerId=${partnere[8].id}") {
                val respondedUttak = json.parse(Uttak.serializer().list, response.content!!)
                val deletedUttak = uttak.filter { it.partner?.id == partnere[8].id }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(deletedUttak, respondedUttak)
                deletedUttak.forEach {
                    assertFalse(UttakRepository.exists(it.id))
                }
            }
        }


        @Test
        fun `delete uttak by partner id and stasjon id`() {
            testDelete("/uttak?partnerId=${partnere[7].id}&stasjonId=${stasjoner[2].id}") {
                val respondedUttak = json.parse(Uttak.serializer().list, response.content!!)
                val deletedUttak =
                    uttak.filter { it.partner?.id == partnere[7].id }.filter { it.stasjon.id == stasjoner[2].id }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(deletedUttak, respondedUttak)
                deletedUttak.forEach {
                    assertFalse(UttakRepository.exists(it.id))
                }
            }
        }

        @Test
        fun `delete uttak from date`() {
            testDelete("/uttak?fromDate=2020-09-06T15:48:06") {
                val respondedUttak = json.parse(Uttak.serializer().list, response.content!!)
                val deletedUttak = uttak.filter { it.startDateTime >= LocalDateTime.parse("2020-09-06T15:48:06") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(deletedUttak, respondedUttak)
                deletedUttak.forEach {
                    assertFalse(UttakRepository.exists(it.id))
                }
            }
        }

        @Test
        fun `delete uttak to date`() {
            testDelete("/uttak?toDate=2020-09-06T15:48:06") {

                val respondedUttak = json.parse(Uttak.serializer().list, response.content!!)
                val deletedUttak = uttak.filter { it.startDateTime <= LocalDateTime.parse("2020-09-06T15:48:06") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(deletedUttak, respondedUttak)
                deletedUttak.forEach {
                    assertFalse(UttakRepository.exists(it.id))
                }
            }
        }

        @Test
        fun `delete uttak in date range`() {
            testDelete("/uttak?fromDate=2020-08-06T15:48:06&toDate=2020-09-06T15:48:06") {

                val respondedUttak = json.parse(Uttak.serializer().list, response.content!!)
                val deletedUttak = uttak.filter { it.startDateTime >= LocalDateTime.parse("2020-08-06T15:48:06") }
                    .filter { it.startDateTime <= LocalDateTime.parse("2020-09-06T15:48:06") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(deletedUttak, respondedUttak)
                deletedUttak.forEach {
                    assertFalse(UttakRepository.exists(it.id))
                }
            }
        }
    }

    @Nested
    inner class Patch {

        @Test
        fun `update uttak start`() {
            val uttakToUpdate = uttak[87]
            val expectedResponse = uttakToUpdate.copy(startDateTime = uttakToUpdate.startDateTime.minusHours(1))
            val body =
                """{
                    "id": "${uttakToUpdate.id}",
                    "startDateTime": "${uttakToUpdate.startDateTime.minusHours(1)}"
                }"""

            testPatch("/uttak", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer(), expectedResponse), response.content)

                val uttakInRepository = UttakRepository.getUttakByID(expectedResponse.id)
                require(uttakInRepository is Either.Right)
                assertEquals(expectedResponse, uttakInRepository.b)

            }
        }

        @Test
        fun `update uttak end`() {
            val uttakToUpdate = uttak[87]
            val expectedResponse = uttakToUpdate.copy(endDateTime = uttakToUpdate.endDateTime.plusHours(1))
            val body =
                """{
                    "id": "${uttakToUpdate.id}",
                    "endDateTime": "${uttakToUpdate.endDateTime.plusHours(1)}"
                }"""

            testPatch("/uttak", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer(), expectedResponse), response.content)

                val uttakInRepository = UttakRepository.getUttakByID(expectedResponse.id)
                require(uttakInRepository is Either.Right)
                assertEquals(expectedResponse, uttakInRepository.b)

            }
        }

        @Test
        fun `update uttak start and end`() {
            val uttakToUpdate = uttak[87]
            val expectedResponse = uttakToUpdate.copy(
                startDateTime = uttakToUpdate.startDateTime.minusHours(1),
                endDateTime = uttakToUpdate.endDateTime.plusHours(1)
            )
            val body =
                """{
                    "id": "${uttakToUpdate.id}",
                    "startDateTime": "${uttakToUpdate.startDateTime.minusHours(1)}",
                    "endDateTime": "${uttakToUpdate.endDateTime.plusHours(1)}"
                }"""

            testPatch("/uttak", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer(), expectedResponse), response.content)

                val uttakInRepository = UttakRepository.getUttakByID(expectedResponse.id)
                require(uttakInRepository is Either.Right)
                assertEquals(expectedResponse, uttakInRepository.b)

            }
        }
    }

    private fun openHours() = mapOf<DayOfWeek, List<LocalTime>>(
        Pair(
            DayOfWeek.MONDAY,
            listOf(
                LocalTime.parse("00:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("23:59:59Z", DateTimeFormatter.ISO_TIME)
            )
        ),
        Pair(
            DayOfWeek.TUESDAY,
            listOf(
                LocalTime.parse("00:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("23:59:59Z", DateTimeFormatter.ISO_TIME)
            )
        ),
        Pair(
            DayOfWeek.WEDNESDAY,
            listOf(
                LocalTime.parse("00:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("23:59:59Z", DateTimeFormatter.ISO_TIME)
            )
        ),
        Pair(
            DayOfWeek.THURSDAY,
            listOf(
                LocalTime.parse("00:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("23:59:59Z", DateTimeFormatter.ISO_TIME)
            )
        ),
        Pair(
            DayOfWeek.FRIDAY,
            listOf(
                LocalTime.parse("00:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("23:59:59Z", DateTimeFormatter.ISO_TIME)
            )
        )
    )
}