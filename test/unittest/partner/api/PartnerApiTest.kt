package partner.api

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
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.partner.form.PartnerGetForm
import ombruk.backend.partner.form.PartnerPostForm
import ombruk.backend.partner.form.PartnerUpdateForm
import ombruk.backend.partner.model.Partner
import ombruk.backend.partner.service.PartnerService
import ombruk.backend.shared.api.JwtMockConfig
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.shared.error.ServiceError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import testutils.testDelete
import testutils.testGet
import testutils.testPatch
import testutils.testPost
import kotlin.test.assertEquals

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
            val expected = Partner(1, "test")

            every { PartnerService.getPartnerById(expected.id) } returns expected.right()

            testGet("/partners/1") {
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

            testGet("/partners/1") {
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

            testGet("/partners/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Id can't be -1.
         */
        @Test
        fun `get partner by id 422`() {
            testGet("/partners/-1") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. Id is not an integer.
         */
        @Test
        fun `get partner by id 400`() {
            testGet("/partners/NaN") {
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
        fun `get partners 200`() {
            val expected = listOf(Partner(1, "test"))

            every { PartnerService.getPartners(PartnerGetForm()) } returns expected.right()

            testGet("/partners") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer().list, expected), response.content)
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `get partners 500`() {
            every { PartnerService.getPartners(PartnerGetForm()) } returns ServiceError("test").left()

            testGet("/partners") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Name cant be blank
         */
        @Test
        fun `get partners 422`() {
            testGet("/partners?name=") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("name: Must not be blank", response.content)
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
            val form = PartnerPostForm("test")
            val expected = Partner(1, "test")
            every { PartnerService.savePartner(form) } returns expected.right()

            testPost("/partners", json.stringify(PartnerPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `post partner 401`() {
            val form = PartnerPostForm("test")

            testPost("/partners", json.stringify(PartnerPostForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when no request doesn't have the required role.
         */
        @Test
        fun `post partner 403`() {
            val form = PartnerPostForm("test")

            testPost("/partners", json.stringify(PartnerPostForm.serializer(), form), JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `post partner 500`() {
            val form = PartnerPostForm("test")

            every { PartnerService.savePartner(form) } returns ServiceError("test").left()

            testPost("/partners", json.stringify(PartnerPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Invalid phone number
         */
        @Test
        fun `post partner 422`() {
            val form = PartnerPostForm("test", phone = "2143")

            testPost("/partners", json.stringify(PartnerPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("phone: has to be valid Norwegian phone number", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. The empty string cannot be parsed.
         */
        @Test
        fun `post partner 400`() {
            testPost("/partners", "") {
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
            val initial = Partner(1, "test")
            val form = PartnerUpdateForm(1, "updated")
            val expected = initial.copy(name = form.name!!)

            every { PartnerService.updatePartner(form) } returns expected.right()
            every { PartnerRepository.getPartners(PartnerGetForm("updated")) } returns listOf<Partner>().right()

            testPatch("/partners", json.stringify(PartnerUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `patch partner 401`() {
            val form = PartnerUpdateForm(1, "updated")

            testPatch("/partners", json.stringify(PartnerUpdateForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when no request doesn't have the required role.
         */
        @Test
        fun `patch partner 403`() {
            val form = PartnerUpdateForm(1, "updated")

            testPatch("/partners", json.stringify(PartnerUpdateForm.serializer(), form), JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `patch partner 500`() {
            val form = PartnerUpdateForm(1, "updated")
            every { PartnerService.updatePartner(form) } returns ServiceError("test").left()
            every { PartnerRepository.getPartners(PartnerGetForm("updated")) } returns listOf<Partner>().right()

            testPatch("/partners", json.stringify(PartnerUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Invalid phone number
         */
        @Test
        fun `patch partner 422`() {
            val form = PartnerUpdateForm(1, "updated", phone = "234")

            testPatch("/partners", json.stringify(PartnerUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("phone: has to be valid Norwegian phone number", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. Id is not an integer.
         */
        @Test
        fun `patch partner 400`() {
            testPatch("/partners", "") {
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
            val expected = Partner(1, "test")
            every { PartnerService.deletePartnerById(1) } returns expected.right()

            testDelete("/partners/1") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `delete partner 401`() {
            testDelete("/partners/1", null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when no request doesn't have the required role.
         */
        @Test
        fun `delete partner 403`() {
            testDelete("/partners/1", JwtMockConfig.partnerBearer2) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `delete partner 500`() {

            every { PartnerService.deletePartnerById(1) } returns ServiceError("test").left()

            testDelete("/partners/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Id can't be -1
         */
        @Test
        fun `delete partner 422`() {

            testDelete("/partners/-1") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. Id is not an integer.
         */
        @Test
        fun `delete partner 400`() {
            testDelete("/partners/NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("id could not be parsed.", response.content)

            }
        }
    }

}