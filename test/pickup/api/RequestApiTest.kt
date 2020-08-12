package pickup.api

import arrow.core.left
import arrow.core.right
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.partner.model.Partner
import ombruk.backend.pickup.database.PickupRepository
import ombruk.backend.pickup.form.request.RequestDeleteForm
import ombruk.backend.pickup.form.request.RequestGetForm
import ombruk.backend.pickup.form.request.RequestPostForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.pickup.model.Request
import ombruk.backend.pickup.service.RequestService
import ombruk.backend.shared.api.JwtMockConfig
import ombruk.backend.shared.database.initDB
import ombruk.backend.shared.error.ServiceError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import testutils.testDelete
import testutils.testGet
import testutils.testPost
import java.time.LocalDateTime
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class RequestApiTest {
    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    init {
        initDB() // Don't want to do this. But EventRepository wont work without it
    }

    @BeforeEach
    fun setup() {
        mockkObject(RequestService)
        mockkObject(PickupRepository)
        mockkObject(PartnerRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Nested
    inner class Get {

        /**
         * Check for 200 when getting valid form
         */
        @Test
        fun `get pickups 200`() {
            val station = Station(1, "test")
            val partner = Partner(1, "test")
            val pickup = Pickup(1, LocalDateTime.now(), LocalDateTime.now(), station = station)
            val expected = listOf(Request(pickup, partner))

            every { RequestService.getRequests(RequestGetForm()) } returns expected.right()

            testGet("/requests") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Request.serializer().list, expected), response.content)
            }
        }

        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `get pickups 500`() {
            every { RequestService.getRequests(RequestGetForm()) }returns ServiceError("test").left()

            testGet("/requests") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Station id can't be -1.
         */
        @Test
        fun `get pickups 422`() {
            testGet("/requests?pickupId=-1") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("pickupId: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. station Id is not an integer.
         */
        @Test
        fun `get pickups 400`() {
            testGet("/requests?pickupId=NaN") {
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
        fun `post pickup 200`() {
            val station = Station(1, "test")
            val partner = Partner(1, "test")
            val pickup = Pickup(1, LocalDateTime.now(), LocalDateTime.now(), station = station)
            val form = RequestPostForm(pickup.id, partner.id)
            val expected = Request(pickup, partner)

            every { RequestService.saveRequest(form) } returns expected.right()
            every { PickupRepository.exists(pickup.id) } returns true
            every { PartnerRepository.exists(partner.id) } returns true

            testPost("/requests", json.stringify(RequestPostForm.serializer(), form), JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Request.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `post pickup 401`() {
            val form = RequestPostForm(1, 1)

            testPost("/requests", json.stringify(RequestPostForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when no request doesn't have the required role.
         */
        @Test
        fun `post pickup 403`() {
            val form = RequestPostForm(1, 1)

            testPost("/requests", json.stringify(RequestPostForm.serializer(), form), JwtMockConfig.partnerBearer2) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `post pickup 500`() {
            val form = RequestPostForm(1, 1)

            every { RequestService.saveRequest(form) } returns ServiceError("test").left()
            every { PartnerRepository.exists(1) } returns true
            every { PickupRepository.exists(1) } returns true


            testPost("/requests", json.stringify(RequestPostForm.serializer(), form), JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Pickup id can't be -1
         */
        @Test
        fun `post pickup 422`() {
            val form = RequestPostForm(-1, 1)

            every { PartnerRepository.exists(1) } returns true
            every { PickupRepository.exists(-1) } returns true

            testPost("/requests", json.stringify(RequestPostForm.serializer(), form), JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("pickupId: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. The empty string cannot be parsed.
         */
        @Test
        fun `post pickup 400`() {
            testPost("/requests", "") {
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
        fun `delete pickup 200`() {
            every { RequestService.deleteRequest(RequestDeleteForm(1, 1))} returns 1.right()

            every { PartnerRepository.exists(1) } returns true
            every { PickupRepository.exists(1) } returns true

            testDelete("/requests?pickupId=1&partnerId=1", JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("1", response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `delete pickup 401`() {
            testDelete("/requests?pickupId=1&partnerId=1", null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when no request doesn't have the required role.
         */
        @Test
        fun `post pickup 403`() {
            testDelete("/requests?pickupId=1&partnerId=1",JwtMockConfig.partnerBearer2) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `post pickup 500`() {
            every { RequestService.deleteRequest(RequestDeleteForm(1, 1)) } returns ServiceError("test").left()
            every { PartnerRepository.exists(1) } returns true
            every { PickupRepository.exists(1) } returns true

            testDelete("/requests?pickupId=1&partnerId=1", JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Pickup id can't be -1
         */
        @Test
        fun `post pickup 422`() {
            every { PartnerRepository.exists(1) } returns true
            every { PickupRepository.exists(-1) } returns true

            testDelete("/requests?pickupId=-1&partnerId=1", JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("pickupId: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. Partner id is not an integer.
         */
        @Test
        fun `post pickup 400`() {
            testDelete("/requests?pickupId=1&partnerId=NaN", JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("partnerId could not be parsed.", response.content)

            }
        }
    }

}