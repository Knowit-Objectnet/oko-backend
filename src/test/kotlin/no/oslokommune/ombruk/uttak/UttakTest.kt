package no.oslokommune.ombruk.uttak

import arrow.core.Either
import arrow.core.extensions.list.functorFilter.filter
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.stringify
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.uttak.form.UttakGetForm
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.stasjon.form.StasjonPostForm
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.uttak.service.UttakService
import no.oslokommune.ombruk.stasjon.service.StasjonService
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.partner.form.PartnerPostForm
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.partner.service.PartnerService
import no.oslokommune.ombruk.shared.database.initDB
import org.junit.jupiter.api.*
import no.oslokommune.ombruk.testDelete
import no.oslokommune.ombruk.testGet
import no.oslokommune.ombruk.testPatch
import no.oslokommune.ombruk.testPost
import no.oslokommune.ombruk.uttak.model.UttaksType
import no.oslokommune.ombruk.uttaksdata.database.UttaksDataRepository
import org.json.simple.JSONArray
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
        UttakRepository.deleteAllUttakForTesting()
    }

    @AfterAll
    fun finish() {
        PartnerRepository.deleteAllPartnereForTesting()
        StasjonRepository.deleteAllStasjonerForTesting()
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
        print(Either.Left)
        require(p is Either.Right)
        return@map p.b
    }

    private fun createTestStasjoner() = (0..5).map {
        val s = StasjonService.saveStasjon(StasjonPostForm("Stasjon$it", aapningstider = openHours()))
        require(s is Either.Right)
        return@map s.b
    }

    private fun createTestUttak(): List<Uttak> {
        var partnerCounter = 0
        var stasjonCounter = 0
        return (1..100L).map {
            val e = UttakService.saveUttak(
                UttakPostForm(
                    stasjoner[stasjonCounter].id,
                    partnere[partnerCounter].id,
                    null,
                    UttaksType.ENKELT,
                    LocalDateTime.parse("2020-07-06T15:48:06").plusDays(it),
                    LocalDateTime.parse("2020-07-06T16:48:06").plusDays(it)
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
            testGet("/uttak?startTidspunkt=2020-08-10T15:00:00") {
                val expected = uttak.filter { it.startTidspunkt >= LocalDateTime.parse("2020-08-10T15:00:00") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get uttak to date`() {
            testGet("/uttak?sluttTidspunkt=2020-08-10T15:00:00") {
                val expected = uttak.filter { it.sluttTidspunkt <= LocalDateTime.parse("2020-08-10T15:00:00") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get uttak in date range`() {
            testGet("/uttak?startTidspunkt=2020-08-10T15:00:00&sluttTidspunkt=2020-09-10T15:00:00") {
                val expected = uttak.filter { it.startTidspunkt >= LocalDateTime.parse("2020-08-10T15:00:00") }
                    .filter { it.startTidspunkt <= LocalDateTime.parse("2020-09-10T15:00:00") }

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
                    "startTidspunkt": "2020-07-06T15:00:00",
                    "sluttTidspunkt": "2020-07-06T16:00:00",
                    "stasjonId": "$stasjonId",
                    "partnerId": "$partnerId"
                }"""

            testPost("/uttak", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responseUttak = json.parse(Uttak.serializer(), response.content!!)
                val insertedUttak = UttakRepository.getUttakByID(responseUttak.id)
                require(insertedUttak is Either.Right)
                assertEquals(responseUttak, insertedUttak.b)
                assertEquals(startDateTime, responseUttak.startTidspunkt)
                assertEquals(endDateTime, responseUttak.sluttTidspunkt)
                assertEquals(stasjonId, responseUttak.stasjon.id)
                assertEquals(partnerId, responseUttak.partner?.id)
            }
        }

        @Test
        fun `create recurring uttak`() {
            val startDateTime = LocalDateTime.parse("2020-07-06T10:00:00")
            val endDateTime = LocalDateTime.parse("2020-07-06T15:00:00")
            val stasjonId = stasjoner[Random.nextInt(1, 5)].id
            val partnerId = partnere[Random.nextInt(1, 9)].id
            val antall = 4

            val body =
                """{
                    "stasjonId": "$stasjonId",
                    "partnerId": "$partnerId",
                    "startTidspunkt": "$startDateTime",
                    "sluttTidspunkt": "$endDateTime",
                    "gjentakelsesRegel": {
                        "until": "2020-08-01T12:00:00",
                        "dager": ["MONDAY"],
                        "intervall": "1",
                        "antall": "$antall"
                    }
                }"""

            testPost("/uttak", body) {
                val responseUttak = json.parse(Uttak.serializer(), response.content!!)
                val insertedUttak =
                    UttakRepository.getUttak(UttakGetForm(gjentakelsesRegelID = responseUttak.gjentakelsesRegel!!.id))

                assertEquals(HttpStatusCode.OK, response.status())
                require(insertedUttak is Either.Right)
                assertTrue { insertedUttak.b.contains(responseUttak) }

                assertEquals(insertedUttak.b.size, antall)

                insertedUttak.b.forEachIndexed { index, uttak ->
                    assertEquals(startDateTime.plusDays(7L * index), uttak.startTidspunkt)
                    assertEquals(endDateTime.plusDays(7L * index), uttak.sluttTidspunkt)
                    assertEquals(stasjonId, uttak.stasjon.id)
                    assertEquals(partnerId, uttak.partner?.id)
                }
            }
        }

        @Test
        fun `create recurring uttak without sluttTidspunkt`() {
            val startDateTime = LocalDateTime.parse("2020-07-06T10:00:00")
            val endDateTime = LocalDateTime.parse("2020-07-06T15:00:00")
            val stasjonId = stasjoner[Random.nextInt(1, 5)].id
            val partnerId = partnere[Random.nextInt(1, 9)].id
            val antall = 4

            val body =
                """{
                    "stasjonId": "$stasjonId",
                    "partnerId": "$partnerId",
                    "startTidspunkt": "$startDateTime",
                    "sluttTidspunkt": "$endDateTime",
                    "gjentakelsesRegel": {
                        "dager": ["MONDAY"],
                        "intervall": "1",
                        "antall": "$antall"
                    }
                }"""

            testPost("/uttak", body) {
                val responseUttak = json.parse(Uttak.serializer(), response.content!!)
                val insertedUttak =
                    UttakRepository.getUttak(UttakGetForm(gjentakelsesRegelID = responseUttak.gjentakelsesRegel!!.id))

                assertEquals(HttpStatusCode.OK, response.status())
                require(insertedUttak is Either.Right)
                assertTrue { insertedUttak.b.contains(responseUttak) }

                assertEquals(insertedUttak.b.size, antall)

                insertedUttak.b.forEachIndexed { index, uttak ->
                    assertEquals(startDateTime.plusDays(7L * index), uttak.startTidspunkt)
                    assertEquals(endDateTime.plusDays(7L * index), uttak.sluttTidspunkt)
                    assertEquals(stasjonId, uttak.stasjon.id)
                    assertEquals(partnerId, uttak.partner?.id)
                }
            }
        }

        @Test
        fun `recurring uttak using antall and intervall 2 skips correctly`() {
            val startDateTime = LocalDateTime.parse("2020-07-06T10:00:00")
            val endDateTime = LocalDateTime.parse("2020-07-06T15:00:00")
            val stasjonId = stasjoner[Random.nextInt(1, 5)].id
            val partnerId = partnere[Random.nextInt(1, 9)].id
            val antall = 2
            val dager = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
            val intervall = 2

            val body =
                """{
                "stasjonId": "$stasjonId",
                "partnerId": "$partnerId",
                "startTidspunkt": "$startDateTime",
                "sluttTidspunkt": "$endDateTime",
                "gjentakelsesRegel": {
                    "dager": $dager,
                    "intervall": "$intervall",
                    "antall": "$antall"
                }
            }"""

            testPost("/uttak", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responseUttak = json.parse(Uttak.serializer(), response.content!!)
                val insertedUttak =
                    UttakRepository.getUttak(UttakGetForm(gjentakelsesRegelID = responseUttak.gjentakelsesRegel!!.id))



                require(insertedUttak is Either.Right)
                assertTrue { insertedUttak.b.contains(responseUttak) }

                assertEquals(antall * dager.size, insertedUttak.b.size)

            }
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `delete uttak by id`() {
            testDelete("/uttak?id=${uttak[68].id}") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertFalse(UttakRepository.exists(uttak[68].id))
            }
        }

        @Test
        fun `delete uttak by stasjon id`() {
            testDelete("/uttak?stasjonId=${stasjoner[1].id}") {
//                val respondedUttak = json.parse(Uttak.serializer().list, response.content!!)
                val text = response.content
                val deletedUttak = uttak.filter { it.stasjon.id == stasjoner[1].id }
                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals(deletedUttak, respondedUttak)
                deletedUttak.forEach {
                    assertFalse(UttakRepository.exists(it.id))
                }
            }
        }

        @Test
        fun `delete uttak by partner id`() {
            testDelete("/uttak?partnerId=${partnere[8].id}") {
                val deletedUttak = uttak.filter { it.partner?.id == partnere[8].id }

                assertEquals(HttpStatusCode.OK, response.status())
                deletedUttak.forEach {
                    assertFalse(UttakRepository.exists(it.id))
                }
            }
        }


        @Test
        fun `delete uttak by partner id and stasjon id`() {
            testDelete("/uttak?partnerId=${partnere[7].id}&stasjonId=${stasjoner[2].id}") {
                //val respondedUttak = json.parse(Uttak.serializer().list, response.content!!)
                val deletedUttak =
                    uttak.filter { it.partner?.id == partnere[7].id }.filter { it.stasjon.id == stasjoner[2].id }

                assertEquals(HttpStatusCode.OK, response.status())
                //assertEquals(deletedUttak, respondedUttak)
                deletedUttak.forEach {
                    assertFalse(UttakRepository.exists(it.id))
                }
            }
        }

        @Test
        fun `delete uttak from date`() {
            testDelete("/uttak?startTidspunkt=2020-09-06T15:48:06") {
                val deletedUttak = uttak.filter { it.startTidspunkt >= LocalDateTime.parse("2020-09-06T15:48:06") }

                assertEquals(HttpStatusCode.OK, response.status())
                deletedUttak.forEach {
                    assertFalse(UttakRepository.exists(it.id))
                }
            }
        }

        @Test
        fun `delete uttak to date`() {
            testDelete("/uttak?sluttTidspunkt=2020-09-06T15:48:06") {

                val deletedUttak = uttak.filter { it.sluttTidspunkt <= LocalDateTime.parse("2020-09-06T15:48:06") }

                assertEquals(HttpStatusCode.OK, response.status())
                deletedUttak.forEach {
                    assertFalse(UttakRepository.exists(it.id))
                }
            }
        }

        @Test
        fun `delete uttak in date range`() {
            testDelete("/uttak?startTidspunkt=2020-08-06T15:48:06&sluttTidspunkt=2020-09-06T15:48:06") {

                val deletedUttak = uttak.filter { it.startTidspunkt >= LocalDateTime.parse("2020-08-06T15:48:06") }
                    .filter { it.sluttTidspunkt <= LocalDateTime.parse("2020-09-06T15:48:06") }

                assertEquals(HttpStatusCode.OK, response.status())
                deletedUttak.forEach {
                    assertFalse(UttakRepository.exists(it.id))
                }
            }
        }
    }

    @Nested
    inner class Patch {

        @Test
        fun `update uttak start and end`() {
            val uttakToUpdate = uttak[87]
            val expectedResponse = uttakToUpdate.copy(
                startTidspunkt = uttakToUpdate.startTidspunkt,
                sluttTidspunkt = uttakToUpdate.sluttTidspunkt
            )
            val body =
                """{
                    "id": "${uttakToUpdate.id}",
                    "startTidspunkt": "${uttakToUpdate.startTidspunkt}",
                    "sluttTidspunkt": "${uttakToUpdate.sluttTidspunkt}"
                }"""

            testPatch("/uttak", body) {
                assertEquals(HttpStatusCode.OK, response.status())

                val actual = json.parse(Uttak.serializer(), response.content!!)
                assertEquals(expectedResponse.startTidspunkt, actual.startTidspunkt)
                assertEquals(expectedResponse.sluttTidspunkt, actual.sluttTidspunkt)

                val uttakInRepository = UttakRepository.getUttakByID(expectedResponse.id)
                require(uttakInRepository is Either.Right)
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