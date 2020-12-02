package no.oslokommune.ombruk.uttak.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.uttak.form.UttakDeleteForm
import no.oslokommune.ombruk.uttak.form.UttakGetForm
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.form.UttakUpdateForm
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.uttak.service.UttakService
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.shared.api.Authorization
import no.oslokommune.ombruk.shared.api.JwtMockConfig
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.shared.error.ServiceError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import no.oslokommune.ombruk.testDelete
import no.oslokommune.ombruk.testGet
import no.oslokommune.ombruk.testPatch
import no.oslokommune.ombruk.testPost
import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import no.oslokommune.ombruk.uttak.model.UttaksType
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

/*
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class UttakAPITest {
    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    @BeforeEach
    fun setup() {
        mockkObject(UttakRepository)
        mockkObject(UttakService)
        mockkObject(StasjonRepository)
        mockkObject(PartnerRepository)
        mockkObject(Authorization)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @AfterAll
    fun finish() {
        unmockkAll()
    }

    @Nested
    inner class GetById {
        /**
         * Check for 200 given a valid id
         */
        @Test
        fun `get single uttak 200`() {
            val s = Stasjon(1, "test")
            val p = Partner(1, "test", "beskrivelse", "81549300", "test@test.com")
            val expected = Uttak(
                id = 1,
                startTidspunkt = LocalDateTime.parse("2020-07-06T10:48:06"),
                sluttTidspunkt = LocalDateTime.parse("2020-07-10T15:48:06"),
                stasjon = s,
                partner = p,
                type = UttaksType.ENKELT)
            every { UttakService.getUttakByID(1) } returns expected.right()

            testGet("/uttak/1") {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }

        /**
         * Check for 404 given an id that is not present
         */
        @Test
        fun `get single uttak 404`() {
            every { UttakService.getUttakByID(1) } returns RepositoryError.NoRowsFound("test").left()

            testGet("/uttak/1") {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals("No rows found with provided ID, test", response.content)
            }
        }

        /**
         * Check for 422 when the id is not valid
         */
        @Test
        fun `get single uttak 422`() {
            testGet("/uttak/0") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 500 when we encounter a serious error
         */
        @Test
        fun `get single uttak 500`() {
            every { UttakService.getUttakByID(1) } returns ServiceError("test").left()

            testGet("/uttak/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }

        /**
         * Check for 400 when we get an id which can't be parsed as an int
         */
        @Test
        fun `get single uttak 400`() {
            testGet("/uttak/NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("id could not be parsed.", response.content)
            }
        }
    }

    @Nested
    inner class Get {

        /**
         * Check for 200 when we try to get all uttak with an empty form
         */
        @Test
        fun `get uttak 200`() {
            val s = Stasjon(1, "test")
            val p = Partner(1, "test", "beskrivelse", "81549300", "test@test.com")
            val e1 = Uttak(1, LocalDateTime.parse("2020-07-06T10:48:06"), LocalDateTime.parse("2020-07-06T15:48:06"), s, p)
            val e2 = e1.copy(2)
            val e3 = e1.copy(3)
            val expected = listOf(e1, e2, e3)

            every { UttakService.getUttak(UttakGetForm()) } returns expected.right()

            testGet("/uttak") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer().list, expected), response.content)
            }
        }

        /**
         * Check for 500 when we encounter a serious error
         */
        @Test
        fun `get uttak 500`() {
            every { UttakService.getUttak(UttakGetForm()) } returns ServiceError("test").left()

            testGet("/uttak") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. uttakId is not a number here.
         */
        @Test
        fun `get uttak 400`() {
            testGet("/uttak?id=NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("id could not be parsed.", response.content)
            }
        }

        /**
         * Check for 422 when we get an invalid form. uttakId is not valid here.
         */
        @Test
        fun `get uttak 422`() {
            testGet("/uttak?id=-1") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }
    }

    @Nested
    inner class Post {

        /**
         * Check for 200 when we post a valid form.
         */
        @Test
        fun `post uttak 200`() {
            val stasjon = Stasjon(id = 1, navn = "test", aapningstider = openHours())
            val partner = Partner(1, "test", "beskrivelse", "81549300", "test@test.com")
            val gjentakelsesRegel = GjentakelsesRegel(
                1,
                LocalDateTime.parse("2020-07-06T10:48:06"),
                LocalDateTime.parse("2020-07-12T15:48:06"),
                null,
                listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
            )
            val form = UttakPostForm(
                stasjon.id,
                partner.id,
                gjentakelsesRegel,
                UttaksType.GJENTAKENDE,
                LocalDateTime.parse("2020-07-06T10:48:06"),
                LocalDateTime.parse("2020-07-06T15:48:06"))
            val expected = Uttak(1, form.startTidspunkt, form.sluttTidspunkt, stasjon, partner)

            every { UttakService.saveUttak(form) } returns expected.right()
            every { PartnerRepository.exists(1) } returns true
            every { StasjonRepository.exists(1) } returns true
            every { StasjonRepository.getStasjonById(1) } returns Either.right(stasjon)

            testPost("/uttak", json.stringify(UttakPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 401 when we don't have a bearer
         */
        @Test
        fun `post uttak 401`() {
            val form = UttakPostForm(
                1,
                1,
                null,
                UttaksType.GJENTAKENDE,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1))

            every { PartnerRepository.exists(1) } returns true
            every { StasjonRepository.exists(1) } returns true

            testPost("/uttak", json.stringify(UttakPostForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when we don't have the required role
         */
        @Test
        fun `post uttak 403`() {
            val form = UttakPostForm(
                1,
                1,
                null,
                UttaksType.GJENTAKENDE,
                LocalDateTime.parse("2020-07-06T10:48:06"),
                LocalDateTime.parse("2020-07-06T15:48:06"))

            every { PartnerRepository.exists(1) } returns true
            every { StasjonRepository.exists(1) } returns true

            testPost("/uttak", json.stringify(UttakPostForm.serializer(), form), JwtMockConfig.partnerBearer2) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `post uttak 500`() {
            val form = UttakPostForm(
                1,
                1,
                null,
                UttaksType.GJENTAKENDE,
                LocalDateTime.parse("2020-07-06T10:48:06"),
                LocalDateTime.parse("2020-07-06T15:48:06"))

            val s = Stasjon(id = 1, navn = "test", aapningstider = openHours())
            every { UttakService.saveUttak(form) } returns ServiceError("test").left()
            every { PartnerRepository.exists(1) } returns true
            every { StasjonRepository.exists(s.id) } returns true
            every { StasjonRepository.getStasjonById(1) } returns Either.right(s)

            testPost("/uttak", json.stringify(UttakPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }

        /**
         * Check for 422 when we get an invalid form. The partner with id 1 does not exist.
         */
        @Test
        fun `post uttak 422`() {
            val form = UttakPostForm(
                stasjonId = 1,
                partnerId = 1,
                type = UttaksType.GJENTAKENDE,
                startTidspunkt = LocalDateTime.parse("2020-07-06T10:48:06"),
                sluttTidspunkt = LocalDateTime.parse("2020-07-06T15:48:06"))

            val s = Stasjon(id = 1, navn = "test", aapningstider = openHours())
            every { PartnerRepository.exists(1) } returns false // Partner does not exist
            every { StasjonRepository.exists(s.id) } returns true
            every { StasjonRepository.getStasjonById(1) } returns Either.right(s)

            testPost("/uttak", json.stringify(UttakPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("partnerId: entity does not exist", response.content)
            }
        }

        /**
         * Check for 400 when we get a form we can't parse. The empty string can't be parsed to our post form.
         */
        @Test
        fun `post uttak 400`() {
            testPost("/uttak", "") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }

    }

    @Nested
    inner class Patch {

        /**
         * Check for 200 when we get a valid patch form.
         */
        @Test
        fun `patch uttak 200`() {
            val startDate = LocalDateTime.parse("2020-07-06T10:48:06")
            val stasjon = Stasjon(1, "test", aapningstider = openHours())
            val partner = Partner(1, "test", "beskrivelse", "81549300", "test@test.com")
            val initial = Uttak(1, startDate.plusDays(2), startDate.plusDays(2).plusHours(1), stasjon, partner)
            val form = UttakUpdateForm(1, startTidspunkt = startDate, sluttTidspunkt = startDate.plusHours(1))
            val expected = initial.copy(startTidspunkt = form.startTidspunkt!!, sluttTidspunkt = form.sluttTidspunkt!!)

            every { UttakService.updateUttak(form) } returns expected.right()
            every { UttakRepository.getUttakByID(1) } returns initial.right()
            every { StasjonRepository.getStasjonById(stasjon.id) } returns Either.right(stasjon)

            testPatch("/uttak", json.stringify(UttakUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Uttak.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `patch uttak 500`() {
            val startDate = LocalDateTime.parse("2020-07-06T10:48:06")
            val stasjon = Stasjon(1, "test", aapningstider = openHours())
            val partner = Partner(1, "test", "beskrivelse", "81549300", "test@test.com")
            val initial = Uttak(1, startDate.plusDays(2), startDate.plusDays(2).plusHours(1), stasjon, partner)
            val form = UttakUpdateForm(1, startTidspunkt = startDate, sluttTidspunkt = startDate.plusHours(1))

            every { UttakService.updateUttak(form) } returns ServiceError("test").left()
            every { UttakRepository.getUttakByID(1) } returns initial.right()
            every { StasjonRepository.getStasjonById(stasjon.id) } returns Either.right(stasjon)

            testPatch("/uttak", json.stringify(UttakUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 401 when no bearer is present
         */
        @Test
        fun `patch uttak 401`() {
            val form = UttakUpdateForm(1, startTidspunkt = LocalDateTime.now(), sluttTidspunkt = LocalDateTime.now().plusHours(1))

            testPatch("/uttak", json.stringify(UttakUpdateForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when we don't have the required role.
         */
        @Test
        fun `patch uttak 403`() {
            val form = UttakUpdateForm(1, startTidspunkt = LocalDateTime.now(), sluttTidspunkt = LocalDateTime.now().plusHours(1))

            testPatch("/uttak", json.stringify(UttakUpdateForm.serializer(), form), JwtMockConfig.partnerBearer2) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. The id can't be -1.
         */
        @Test
        fun `patch uttak 422`() {
            val stasjon = Stasjon(1, "test")
            val partner = Partner(1, "test", "beskrivelse", "81549300", "test@test.com")
            val initial = Uttak(1, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), stasjon, partner)
            val form = UttakUpdateForm(-1, startTidspunkt = LocalDateTime.now(), sluttTidspunkt = LocalDateTime.now().plusHours(1))
            val expected = initial.copy(startTidspunkt = form.startTidspunkt!!, sluttTidspunkt = form.sluttTidspunkt!!)

            every { UttakService.updateUttak(form) } returns expected.right()
            every { UttakRepository.getUttakByID(1) } returns initial.right()

            testPatch("/uttak", json.stringify(UttakUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)

            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed.
         * The empty string can't be parsed to out patch form.
         */
        @Test
        fun `patch uttak 400`() {
            testPatch("/uttak", "") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Nested
    inner class Delete {

        /**
         * Check for 200 when we get a valid delete form.
         */
        @Test
        fun `delete uttak by id 200`() {
            val stasjon = Stasjon(1, "test")
            val partner = Partner(1, "test", "beskrivelse", "81549300", "test@test.com")
            val expected = Unit

            every { UttakService.deleteUttakById(1) } returns Unit.right()

            testDelete("/uttak?id=1") {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }

        /*
        // TODO: Make all these run green!
        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `delete uttak 500`() {
            every { UttakService.deleteUttak(UttakDeleteForm(1)) } returns ServiceError("test").left()

            testDelete("/uttak") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `delete uttak 401`() {
            testDelete("/uttak", null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 404 when there is no uttak that match our partner id. This is only "semi intended" behaviour
         * Someone please fix later. @todo FIX this
         */
        @Test
        fun `delete uttak 404`() {
            val s = Stasjon(1, "test")
            val p = Partner(1, "test", "beskrivelse", "81549300", "test@test.com")
            val expected = listOf(Uttak(1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), s, p))

            every { UttakService.getUttak(UttakGetForm()) } returns expected.right()
            every { UttakService.deleteUttak(UttakDeleteForm(1)) } returns Unit.right()

            testDelete("/uttak", JwtMockConfig.partnerBearer2) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }

        /**
         * Check for 422 when form is invalid. uttakId can't be -1.
         */
        @Test
        fun `delete uttak 422`() {
            testDelete("/uttak?uttakId=-1") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("uttakId: Must be greater than 0", response.content)

            }
        }

        /**
         * Check for 400 when we get a form that can't be parsed. uttakId has to be an int.
         */
        @Test
        fun `delete uttak 400`() {
            testDelete("/uttak?uttakId=NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("uttakId could not be parsed.", response.content)

            }
        }
         */
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
 */