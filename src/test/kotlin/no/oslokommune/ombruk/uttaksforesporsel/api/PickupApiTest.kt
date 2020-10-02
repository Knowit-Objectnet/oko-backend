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
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.uttaksforesporsel.database.PickupRepository
import no.oslokommune.ombruk.uttaksforesporsel.form.pickup.*
import no.oslokommune.ombruk.uttaksforesporsel.model.Pickup
import no.oslokommune.ombruk.uttaksforesporsel.service.PickupService
import no.oslokommune.ombruk.shared.api.JwtMockConfig
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.shared.error.ServiceError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import no.oslokommune.ombruk.testDelete
import no.oslokommune.ombruk.testGet
import no.oslokommune.ombruk.testPatch
import no.oslokommune.ombruk.testPost
import java.time.LocalDateTime
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class UttakAPITest {
    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    @BeforeEach
    fun setup() {
        mockkObject(PickupService)
        mockkObject(StasjonRepository)
        mockkObject(PickupRepository)
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
    inner class GetById {

        /**
         * Check for 200 when getting valid id.
         */
        @Test
        fun `get pickup by id 200`() {
            val s = Stasjon(1, "test")
            val expected = Pickup(1, LocalDateTime.now(), LocalDateTime.now(), stasjon = s)

            every { PickupService.getPickupById(PickupGetByIdForm(1)) } returns expected.right()

            testGet("/pickups/1") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 404 when getting id which doesn't exist
         */
        @Test
        fun `get pickup by id 404`() {
            every { PickupService.getPickupById(PickupGetByIdForm(1)) } returns RepositoryError.NoRowsFound("test")
                .left()

            testGet("/pickups/1") {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals("No rows found with provided ID, test", response.content)
            }
        }

        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `get pickup by id 500`() {
            every { PickupService.getPickupById(PickupGetByIdForm(1)) } returns ServiceError("test").left()

            testGet("/pickups/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Id can't be -1.
         */
        @Test
        fun `get pickup by id 422`() {
            testGet("/pickups/-1") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. Id is not an integer.
         */
        @Test
        fun `get pickup by id 400`() {
            testGet("/pickups/NaN") {
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
        fun `get pickups 200`() {
            val s = Stasjon(1, "test")
            val expected = listOf(Pickup(1, LocalDateTime.now(), LocalDateTime.now(), stasjon = s))

            every { PickupService.getPickups(PickupGetForm()) } returns expected.right()

            testGet("/pickups") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer().list, expected), response.content)
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `get pickups 500`() {
            every { PickupService.getPickups(PickupGetForm()) } returns ServiceError("test").left()

            testGet("/pickups") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Stasjon id can't be -1.
         */
        @Test
        fun `get pickups 422`() {
            testGet("/pickups?stasjonId=-1") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("stasjonId: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. stasjon Id is not an integer.
         */
        @Test
        fun `get pickups 400`() {
            testGet("/pickups?stasjonId=NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("stasjonId could not be parsed.", response.content)
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
            val s = Stasjon(1, "test")
            val form = PickupPostForm(LocalDateTime.now(), LocalDateTime.now().plusDays(1), "test", 1)
            val expected = Pickup(1, form.startDateTime, form.endDateTime, stasjon = s)

            every { PickupService.savePickup(form) } returns expected.right()
            every { StasjonRepository.exists(1) } returns true

            testPost("/pickups", json.stringify(PickupPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `post pickup 401`() {
            val form = PickupPostForm(LocalDateTime.now(), LocalDateTime.now().plusDays(1), "test", 1)

            testPost("/pickups", json.stringify(PickupPostForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when no uttaksforesporsel doesn't have the required role.
         */
        @Test
        fun `post pickup 403`() {
            val form = PickupPostForm(LocalDateTime.now(), LocalDateTime.now().plusDays(1), "test", 1)

            testPost("/pickups", json.stringify(PickupPostForm.serializer(), form), JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `post pickup 500`() {
            val form = PickupPostForm(LocalDateTime.now(), LocalDateTime.now().plusDays(1), "test", 1)

            every { PickupService.savePickup(form) } returns ServiceError("test").left()
            every { StasjonRepository.exists(1) } returns true


            testPost("/pickups", json.stringify(PickupPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. End date can't be fore start date
         */
        @Disabled
        @Test
        fun `post pickup 422`() {
            val form = PickupPostForm(LocalDateTime.now(), LocalDateTime.now().minusDays(1), "test", 1)

            every { StasjonRepository.exists(1) } returns true

            testPost("/pickups", json.stringify(PickupPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("startDateTime: has to be less than end datetime", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. The empty string cannot be parsed.
         */
        @Test
        fun `post pickup 400`() {
            testPost("/pickups", "") {
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
        fun `patch pickup 200`() {
            val s = Stasjon(1, "test")
            val initial = Pickup(1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), stasjon = s)
            val form = PickupUpdateForm(1, LocalDateTime.now(), LocalDateTime.now().plusDays(1))
            val expected = initial.copy(startDateTime = form.startDateTime!!, endDateTime = form.endDateTime!!)

            every { PickupRepository.getPickupById(1) } returns initial.right()
            every { PickupService.updatePickup(form) } returns expected.right()
            every { StasjonRepository.exists(1) } returns true

            testPatch("/pickups", json.stringify(PickupUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Pickup.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `patch pickup 401`() {
            val form = PickupUpdateForm(1, LocalDateTime.now())

            testPatch("/pickups", json.stringify(PickupUpdateForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when no uttaksforesporsel doesn't have the required role.
         */
        @Test
        fun `patch pickup 403`() {
            val form = PickupUpdateForm(1, LocalDateTime.now())

            testPatch("/pickups", json.stringify(PickupUpdateForm.serializer(), form), JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `patch pickup 500`() {
            val form = PickupUpdateForm(1, LocalDateTime.now())

            every { PickupService.updatePickup(form) } returns ServiceError("test").left()
            every { StasjonRepository.exists(1) } returns true


            testPatch("/pickups", json.stringify(PickupUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. End date can't be fore start date
         */
        @Disabled
        @Test
        fun `patch pickup 422`() {
            val s = Stasjon(1, "test")
            val initial = Pickup(1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), stasjon = s)
            val form = PickupUpdateForm(1, LocalDateTime.now(), LocalDateTime.now().minusDays(1))

            every { PickupRepository.getPickupById(1) } returns initial.right()
            every { StasjonRepository.exists(1) } returns true

            testPatch("/pickups", json.stringify(PickupUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals(
                    "startDateTime: has to be less than end datetime, endDateTime: has to be greater than start datetime",
                    response.content
                )
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. Id is not an integer.
         */
        @Test
        fun `patch pickup 400`() {
            testPatch("/pickups", "") {
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
            every { PickupService.deletePickup(PickupDeleteForm(1)) } returns 1.right()

            testDelete("/pickups/1") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("1", response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `delete pickup 401`() {
            testDelete("/pickups/1", null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when no uttaksforesporsel doesn't have the required role.
         */
        @Test
        fun `delete pickup 403`() {
            testDelete("/pickups/1", JwtMockConfig.partnerBearer2) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }


        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `delete pickup 500`() {

            every { PickupService.deletePickup(PickupDeleteForm(1)) } returns ServiceError("test").left()

            testDelete("/pickups/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. Id can't be -1
         */
        @Test
        fun `delete pickup 422`() {

            testDelete("/pickups/-1") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. Id is not an integer.
         */
        @Test
        fun `delete pickup 400`() {
            testDelete("/pickups/NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("id could not be parsed.", response.content)

            }
        }
    }

}