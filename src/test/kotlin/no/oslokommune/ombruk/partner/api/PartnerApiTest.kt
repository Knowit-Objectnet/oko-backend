package no.oslokommune.ombruk.partner.api

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
import no.oslokommune.ombruk.partner.form.PartnerGetForm
import no.oslokommune.ombruk.partner.form.PartnerPostForm
import no.oslokommune.ombruk.partner.form.PartnerUpdateForm
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.partner.service.PartnerService
import no.oslokommune.ombruk.shared.api.JwtMockConfig
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.shared.error.ServiceError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import no.oslokommune.ombruk.testDelete
import no.oslokommune.ombruk.testGet
import no.oslokommune.ombruk.testPatch
import no.oslokommune.ombruk.testPost
import kotlin.test.assertEquals

/*
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class PartnerApiTest {
    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    @BeforeEach
    fun setup() {
        mockkObject(PartnerService)
        mockkObject(PartnerRepository)
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
         * Check for 200 when getting valid id.
         */
        @Test
        fun `get partner by id 200`() {
            val expected = Partner(1, "test", "beskrivelse", "81549300", "test@test.com")

            every { PartnerService.getPartnerById(expected.id) } returns expected.right()

            testGet("/partnere/1") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 404 when getting id which doesn't exist
         */
        @Test
        fun `get partner by id 404`() {
            every { PartnerService.getPartnerById(1) } returns RepositoryError.NoRowsFound("test")
                .left()

            testGet("/partnere/1") {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals("No rows found with provided ID, test", response.content)
            }
        }

        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `get partner by id 500`() {
            every { PartnerService.getPartnerById(1) } returns ServiceError("test").left()

            testGet("/partnere/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Id can't be -1.
         */
        @Test
        fun `get partner by id 422`() {
            testGet("/partnere/-1") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. Id is not an integer.
         */
        @Test
        fun `get partner by id 400`() {
            testGet("/partnere/NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("id could not be parsed.", response.content)
            }
        }
    }

    @Nested
    inner class Get {

        /**
         * Check for 200 when getting valid form
         */
        @Test
        fun `get partnere 200`() {
            val expected = listOf(Partner(1, "test", "beskrivelse", "81549300", "test@test.com"))

            every { PartnerService.getPartnere(PartnerGetForm()) } returns expected.right()

            testGet("/partnere") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer().list, expected), response.content)
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `get partnere 500`() {
            every { PartnerService.getPartnere(PartnerGetForm()) } returns ServiceError("test").left()

            testGet("/partnere") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Name cant be blank
         */
        @Test
        fun `get partnere 422`() {
            testGet("/partnere?navn=") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                var text = response.content
                assertEquals("navn: Must not be blank", response.content)
            }
        }
    }

    @Nested
    inner class Post {

        /**
         * Check for 200 when getting valid form
         */
        @Test
        fun `post partner 200`() {
            val form = PartnerPostForm("test", "beskrivelse", "81549300", "test@test.com")
            val expected = Partner(1, "test", "beskrivelse", "81549300", "test@test.com")
            every { PartnerService.savePartner(form) } returns expected.right()

            testPost("/partnere", json.stringify(PartnerPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `post partner 401`() {
            val form = PartnerPostForm("test", "beskrivelse", "81549300", "test@test.com")

            testPost("/partnere", json.stringify(PartnerPostForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when no uttaksforesporsel doesn't have the required role.
         */
        @Test
        fun `post partner 403`() {
            val form = PartnerPostForm("test", "beskrivelse", "81549300", "test@test.com")

            testPost("/partnere", json.stringify(PartnerPostForm.serializer(), form), JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `post partner 500`() {
            val form = PartnerPostForm("test", "beskrivelse", "81549300", "test@test.com")

            every { PartnerService.savePartner(form) } returns ServiceError("test").left()

            testPost("/partnere", json.stringify(PartnerPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Invalid phone number
         */
        @Test
        fun `post partner 422`() {
            val form = PartnerPostForm("test", "beskrivelse", "2113", "test@test.com")

            testPost("/partnere", json.stringify(PartnerPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("telefon: has to be valid Norwegian phone number", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. The empty string cannot be parsed.
         */
        @Test
        fun `post partner 400`() {
            testPost("/partnere", "") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Nested
    inner class Patch {

        /**
         * Check for 200 when getting valid form
         */
        @Test
        fun `patch partner 200`() {
            val initial = Partner(1, "test", "beskrivelse", "81549300", "test@test.com")
            val form = PartnerUpdateForm(1, "updated", "beskrivelse", "81549300", "test@test.com")
            val expected = initial.copy(navn = form.navn!!)

            every { PartnerService.updatePartner(form) } returns expected.right()
            every { PartnerRepository.getPartnere(PartnerGetForm("updated")) } returns listOf<Partner>().right()

            testPatch("/partnere", json.stringify(PartnerUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `patch partner 401`() {
            val form = PartnerUpdateForm(1, "navn", "beskrivelse", "81549300", "test@test.com")

            testPatch("/partnere", json.stringify(PartnerUpdateForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when no uttaksforesporsel doesn't have the required role.
         */
        @Test
        fun `patch partner 403`() {
            val form = PartnerUpdateForm(1, "navn", "beskrivelse", "81549300", "test@test.com")

            testPatch("/partnere", json.stringify(PartnerUpdateForm.serializer(), form), JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `patch partner 500`() {
            val form = PartnerUpdateForm(1, "navn", "beskrivelse", "81549300", "test@test.com")
            every { PartnerService.updatePartner(form) } returns ServiceError("test").left()
            every { PartnerRepository.getPartnere(PartnerGetForm("updated")) } returns listOf<Partner>().right()

            testPatch("/partnere", json.stringify(PartnerUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Invalid phone number
         */
        @Test
        fun `patch partner 422`() {
            val form = PartnerUpdateForm(1, "navn", "beskrivelse", "213", "test@test.com")

            testPatch("/partnere", json.stringify(PartnerUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("telefon: has to be valid Norwegian phone number", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. Id is not an integer.
         */
        @Test
        fun `patch partner 400`() {
            testPatch("/partnere", "") {
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
        fun `delete partner 200`() {
            val expected = Partner(1, "test", "beskrivelse", "81549300", "test@test.com")
            every { PartnerService.deletePartnerById(1) } returns expected.right()

            testDelete("/partnere/1") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `delete partner 401`() {
            testDelete("/partnere/1", null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when no uttaksforesporsel doesn't have the required role.
         */
        @Test
        fun `delete partner 403`() {
            testDelete("/partnere/1", JwtMockConfig.partnerBearer2) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `delete partner 500`() {

            every { PartnerService.deletePartnerById(1) } returns ServiceError("test").left()

            testDelete("/partnere/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Id can't be -1
         */
        @Test
        fun `delete partner 422`() {

            testDelete("/partnere/-1") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. Id is not an integer.
         */
        @Test
        fun `delete partner 400`() {
            testDelete("/partnere/NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("id could not be parsed.", response.content)

            }
        }
    }

}
 */