package no.oslokommune.ombruk.uttaksforesporsel.api

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
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.shared.api.JwtMockConfig
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.testDelete
import no.oslokommune.ombruk.testGet
import no.oslokommune.ombruk.testPost
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksforesporsel.database.UttaksforesporselRepository
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksForesporselGetForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselDeleteForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselPostForm
import no.oslokommune.ombruk.uttaksforesporsel.model.UttaksForesporsel
import no.oslokommune.ombruk.uttaksforesporsel.service.UttaksforesporselService
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class UttaksforesporselApiTest {

    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    @BeforeEach
    fun setup() {
        mockkObject(UttaksforesporselService)
        mockkObject(UttakRepository)
        mockkObject(PartnerRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @AfterAll
    fun finish() {
        unmockkAll()
    }

    @Nested
    inner class Get {

        /**
         * Check for 200 when getting valid form
         */
        @Test
        fun `get requests 200`() {
            val stasjon = Stasjon(1, "test")
            val partner = Partner(1, "test", "TestPartner", "12345678", "test@test.com")
//            val pickup = Pickup(1, LocalDateTime.now(), LocalDateTime.now(), stasjon = stasjon)
            val uttak = Uttak(
                1,
                LocalDateTime.parse("2020-07-07T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-07T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                stasjon,
                partner
            )
            val expected = listOf(UttaksForesporsel(uttak, partner))

            every { UttaksforesporselService.getRequests(UttaksForesporselGetForm()) } returns expected.right()

            testGet("/uttaksforesporsel") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(UttaksForesporsel.serializer().list, expected), response.content)
            }
        }

        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `get requests 500`() {
            every { UttaksforesporselService.getRequests(UttaksForesporselGetForm()) } returns ServiceError("test").left()

            testGet("/uttaksforesporsel") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Stasjon id can't be -1.
         */
        @Test
        fun `get requests 422`() {
            testGet("/uttaksforesporsel?pickupId=-1") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("pickupId: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. stasjon Id is not an integer.
         */
        @Test
        fun `get requests 400`() {
            testGet("/uttaksforesporsel?pickupId=NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("pickupId could not be parsed.", response.content)
            }
        }
    }

    @Nested
    inner class Post {

        /**
         * Check for 200 when getting valid form
         */
        @Test
        fun `post request 200`() {
            val stasjon = Stasjon(1, "test")
            val partner = Partner(1, "test", "TestPartner", "12345678", "test@test.com")
            val uttak = Uttak(
                1,
                LocalDateTime.parse("2020-07-07T16:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-07T18:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                stasjon,
                partner
            )
            val form = UttaksforesporselPostForm(uttak.id, partner.id)
            val expected = UttaksForesporsel(uttak, partner)

            every { UttaksforesporselService.saveRequest(form) } returns expected.right()
            every { UttakRepository.exists(uttak.id) } returns true
            every { PartnerRepository.exists(partner.id) } returns true

            testPost(
                "/uttaksforesporsel",
                json.stringify(UttaksforesporselPostForm.serializer(), form),
                JwtMockConfig.partnerBearer1
            ) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(UttaksForesporsel.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `post request 401`() {
            val form = UttaksforesporselPostForm(1, 1)

            testPost("/uttaksforesporsel", json.stringify(UttaksforesporselPostForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when no uttaksforesporsel doesn't have the required role.
         */
        @Test
        fun `post request 403`() {
            val form = UttaksforesporselPostForm(1, 1)

            testPost(
                "/uttaksforesporsel",
                json.stringify(UttaksforesporselPostForm.serializer(), form),
                JwtMockConfig.partnerBearer2
            ) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `post request 500`() {
            val form = UttaksforesporselPostForm(1, 1)

            every { PartnerRepository.exists(1) } returns true
            every { UttakRepository.exists(1) } returns true
            every { UttaksforesporselService.saveRequest(form) } returns ServiceError("test").left()
//            every { PickupRepository.exists(1) } returns true


            testPost(
                "/uttaksforesporsel",
                json.stringify(UttaksforesporselPostForm.serializer(), form),
                JwtMockConfig.partnerBearer1
            ) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Pickup id can't be -1
         */
        @Test
        fun `post request 422`() {
            val form = UttaksforesporselPostForm(-1, 1)

            every { PartnerRepository.exists(1) } returns true
            every { UttakRepository.exists(-1) } returns true

            testPost(
                "/uttaksforesporsel",
                json.stringify(UttaksforesporselPostForm.serializer(), form),
                JwtMockConfig.partnerBearer1
            ) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("uttaksId: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. The empty string cannot be parsed.
         */
        @Test
        fun `post request 400`() {
            testPost("/uttaksforesporsel", "") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Nested
    inner class Delete {

        /**
         * Check for 200 when getting valid form
         */
        @Test
        fun `delete request 200`() {
            every { UttaksforesporselService.deleteRequest(UttaksforesporselDeleteForm(1, 1)) } returns 1.right()

            every { PartnerRepository.exists(1) } returns true
            every {UttakRepository.exists(1)} returns true

            testDelete("/uttaksforesporsel?uttaksId=1&partnerId=1", JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("1", response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `delete request 401`() {
            testDelete("/uttaksforesporsel?uttaksId=1&partnerId=1", null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when no uttaksforesporsel doesn't have the required role.
         */
        @Test
        fun `delete request 403`() {
            testDelete("/uttaksforesporsel?uttaksId=1&partnerId=1", JwtMockConfig.partnerBearer2) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `delete request 500`() {
            every {
                UttaksforesporselService.deleteRequest(
                    UttaksforesporselDeleteForm(
                        1,
                        1
                    )
                )
            } returns ServiceError("test").left()
            every { PartnerRepository.exists(1) } returns true
            every { UttakRepository.exists(1) } returns true

            testDelete("/uttaksforesporsel?uttaksId=1&partnerId=1", JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Pickup id can't be -1
         */
        @Test
        fun `delete request 422`() {
            every { PartnerRepository.exists(1) } returns true
            every { UttakRepository.exists(-1) } returns true

            testDelete("/uttaksforesporsel?uttaksId=-1&partnerId=1", JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("uttaksId: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. Partner id is not an integer.
         */
        @Test
        fun `delete request 400`() {
            testDelete("/uttaksforesporsel?uttaksId=1&partnerId=NaN", JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("partnerId could not be parsed.", response.content)

            }
        }
    }


}