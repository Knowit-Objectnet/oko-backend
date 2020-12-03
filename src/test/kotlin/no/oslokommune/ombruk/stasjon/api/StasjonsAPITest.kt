package no.oslokommune.ombruk.stasjon.api

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
import no.oslokommune.ombruk.stasjon.form.StasjonGetForm
import no.oslokommune.ombruk.stasjon.form.StasjonPostForm
import no.oslokommune.ombruk.stasjon.form.StasjonUpdateForm
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.stasjon.service.StasjonService
import no.oslokommune.ombruk.shared.api.Authorization
import no.oslokommune.ombruk.shared.api.JwtMockConfig
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.shared.error.ValidationError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import no.oslokommune.ombruk.testDelete
import no.oslokommune.ombruk.testGet
import no.oslokommune.ombruk.testPatch
import no.oslokommune.ombruk.testPost
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

/*
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class StasjonerAPITest {
    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    @BeforeEach
    fun setup() {
        mockkObject(StasjonRepository)
        mockkObject(StasjonService)
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

        /*
        Check for 200 given a valid ID
         */
        @Test
        fun `get single uttak 200`() {
            val expected = Stasjon(1, "test")
            every { StasjonService.getStasjonById(1) } returns expected.right()

            testGet("/stasjoner/1") {
                val receivedStasjon: Stasjon = json.parse(Stasjon.serializer(), response.content!!)
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(expected.navn, receivedStasjon.navn)
            }
        }

        /*
        Getting a nonexisting uttak should return a 404
         */
        @Test
        fun `get nonexisting uttak 404`() {
            every { StasjonService.getStasjonById(1) } returns RepositoryError.NoRowsFound("test").left()

            testGet("/stasjoner/1") {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals("No rows found with provided ID, test", response.content)
            }
        }

        /*
        Id cannot be lower than 0, should return 422.
         */
        @Test
        fun `get with unacceptable input 422`() {
            testGet("/stasjoner/0") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }

        /*
        Test for 400 when input can't be parsed to int
         */
        @Test
        fun `get with unprocessable input 400`() {
            testGet("/stasjoner/NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("id could not be parsed.", response.content)
            }
        }

        /*
        Check for 500 when we encounter a serious error
         */
        @Test
        fun `get single stasjon 500`() {
            every { StasjonService.getStasjonById(1) } returns ServiceError("test").left()

            testGet("/stasjoner/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }
    }

    @Nested
    inner class Get {

        /*
        Get all stasjoner
         */
        @Test
        fun `get stasjoner 200`() {
            val s = Stasjon(1, "Test 1")
            val s2 = Stasjon(2, "Test 2")
            val s3 = Stasjon(3, "Test 3")
            val expected = listOf(s, s2, s3)

            every { StasjonService.getStasjoner(StasjonGetForm()) } returns expected.right()

            testGet("/stasjoner/") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Stasjon.serializer().list, expected), response.content)
            }
        }

        /*
        The name parameter should ensure that returned stasjoner match the parameter value.
         */
        @Test
        fun `get stasjon by name`() {
            val s = Stasjon(1, "Test 1")
            val s2 = Stasjon(2, "Test 2")
            val s3 = Stasjon(3, "Test 3")
            val expected = listOf(s2)

            every { StasjonService.getStasjoner(StasjonGetForm("Test 2")) } returns expected.right()

            testGet("/stasjoner/?navn=Test+2") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Stasjon.serializer().list, expected), response.content)
            }
        }

        /*
        A name parameter value that matches no stasjoner should return an empty list
         */
        @Test
        fun `get stasjon by name empty array`() {
            val s = Stasjon(1, "Test 1")
            val s2 = Stasjon(2, "Test 2")
            val s3 = Stasjon(3, "Test 3")
            val expected = emptyList<Stasjon>()

            every { StasjonService.getStasjoner(StasjonGetForm("Test")) } returns expected.right()

            testGet("/stasjoner/?name=Test") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("[\n]", response.content)
            }
        }

        /*
        Check for 500 when we encounter a serious error
         */
        @Test
        fun `get stasjoner 500`() {
            every { StasjonService.getStasjoner(StasjonGetForm()) } returns ServiceError("test").left()

            testGet("/stasjoner/") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }
    }

    @Nested
    inner class Post {

        /*
           A valid post should return 200
        */
        @Test
        fun `post stasjon 200`() {
            val hours = mapOf<DayOfWeek, List<LocalTime>>(
                Pair(
                    DayOfWeek.MONDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.TUESDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.WEDNESDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.FRIDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                )
            )
            val form = StasjonPostForm("Haraldrud", hours)
            val expected = Stasjon(1, "Haraldrud", hours)

            every { StasjonRepository.exists(1) } returns false
            every { StasjonService.saveStasjon(form) } returns expected.right()

            testPost("/stasjoner/", json.stringify(StasjonPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Stasjon.serializer(), expected), response.content)
            }
        }

        /*
    Invalid Days should return a 422 unprocessable
     */
        @Test
        fun `post invalid days 422`() {
            val hours = mapOf<DayOfWeek, List<LocalTime>>(
                Pair(
                    DayOfWeek.MONDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.TUESDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.WEDNESDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.THURSDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.FRIDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                ),
                Pair(
                    DayOfWeek.SATURDAY,
                    listOf(
                        LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                        LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                    )
                )
            )
            val form = StasjonPostForm("Haraldrud", hours)

            every { StasjonRepository.exists(1) } returns false
            every { StasjonService.saveStasjon(form) } returns ValidationError.Unprocessable("test").left()

            testPost("/stasjoner/", json.stringify(StasjonPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                println(response.content)
            }
        }

        /*
    Check if missing bearer returns a 401 unauthorized
     */
        @Test
        fun `post without bearer 401`() {
            val hours = mapOf<DayOfWeek, List<LocalTime>>(
                Pair(DayOfWeek.MONDAY, listOf(
                    LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                    LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                ))
            )
            val form = StasjonPostForm("Haraldrud", hours)

            every { StasjonRepository.exists(1) } returns false

            testPost("/stasjoner/", json.stringify(StasjonPostForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /*
    Mangled JWT should return 401
     */
        @Test
        fun `post with invalid bearer 401`() {
            val hours = mapOf<DayOfWeek, List<LocalTime>>(
                Pair(DayOfWeek.MONDAY, listOf(
                    LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                    LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                ))
            )
            val form = StasjonPostForm("Haraldrud", hours)

            every { StasjonRepository.exists(1) } returns false

            testPost(
                "/stasjoner/",
                json.stringify(StasjonPostForm.serializer(), form),
                JwtMockConfig.regEmployeeBearer.drop(5)
            ) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /*
    Partnere should not be able to post stasjoner
     */
        @Test
        fun `post stasjon as partner 403`() {
            val hours = mapOf<DayOfWeek, List<LocalTime>>(
                Pair(DayOfWeek.MONDAY, listOf(
                    LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                    LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                ))
            )
            val form = StasjonPostForm("Haraldrud", hours)

            every { StasjonRepository.exists(1) } returns false

            testPost("/stasjoner/", json.stringify(StasjonPostForm.serializer(), form), JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /*
    Reuse stasjon workers should not be able to post stasjoner
     */
        @Test
        fun `post stasjon as reuse stasjon 403`() {
            val hours = mapOf<DayOfWeek, List<LocalTime>>(
                Pair(DayOfWeek.MONDAY, listOf(
                    LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                    LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
                ))
            )
            val form = StasjonPostForm("Haraldrud", hours)

            every { StasjonRepository.exists(1) } returns false

            testPost(
                "/stasjoner/",
                json.stringify(StasjonPostForm.serializer(), form),
                JwtMockConfig.reuseStasjonBearer
            ) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /*
    Invalid JSON should return a 400
     */
        @Test
        fun `post stasjon invalid json 400`() {
            val testJson = """ {"name": "Haraldrud", "test": "test"} """

            every { StasjonRepository.exists(1) } returns false

            testPost("/stasjoner/", testJson) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Nested
    inner class Patch {

        /*
        Check for 200 when patch is valid
         */
        @Test
        fun `patch stasjon 200`() {
            val form = StasjonUpdateForm(1, "Test1")
            val initial = Stasjon(1, "Test")
            val expected = Stasjon(1, "Test1")

            every { StasjonService.getStasjonById(1) } returns initial.right()
            every { StasjonService.updateStasjon(form) } returns expected.right()

            testPatch("/stasjoner/", json.stringify(StasjonUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Stasjon.serializer(), expected), response.content)
            }
        }

        /*
        Check for 401 when no bearer is present
         */
        @Test
        fun `patch stasjon no bearer 401`() {
            val form = StasjonUpdateForm(1, "Test")
            testPatch("/stasjoner/", json.stringify(StasjonUpdateForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /*
        Check for 403 when partner tries to update stasjon
         */
        @Test
        fun `patch stasjon as partner 403`() {
            val form = StasjonUpdateForm(1, "Test")
            testPatch(
                "/stasjoner/",
                json.stringify(StasjonUpdateForm.serializer(), form),
                JwtMockConfig.partnerBearer2
            ) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /*
        Check for 403 when stasjon worker tries to update stasjon
         */
        @Test
        fun `patch stasjon as stasjon worker 403`() {
            val form = StasjonUpdateForm(1, "Test")
            testPatch(
                "/stasjoner/",
                json.stringify(StasjonUpdateForm.serializer(), form),
                JwtMockConfig.reuseStasjonBearer
            ) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /*
        Check for 500 when we encounter a serious error
         */
        @Test
        fun `patch stasjon 500`() {
            val form = StasjonUpdateForm(1, "Test")
            every { StasjonService.updateStasjon(form) } returns ServiceError("test").left()

            testPatch("/stasjoner/", json.stringify(StasjonUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /*
        Check for 404 when stasjon does not exist
         */
        @Test
        fun `patch stasjon 404`() {
            val form = StasjonUpdateForm(1, "Test")
            every { StasjonService.updateStasjon(form) } returns RepositoryError.NoRowsFound("1").left()

            testPatch("/stasjoner/", json.stringify(StasjonUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }

        /*
        Stasjoner that cannot be deserialized should return a 400
         */
        @Test
        fun `patch stasjon invalid json 400`() {
            val testJson = """ {"id": "NaN", "name": "tester"} """

            testPatch("/stasjoner/", testJson) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }

        /*
        Check for 422 when form is invalid
         */
        fun `patch stasjon invalid state`() {
            val form = StasjonUpdateForm(0, "Test")

            testPatch("/stasjoner/2", json.stringify(StasjonUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
            }
        }


    }

    @Nested
    inner class Delete {

        /*
        A successful delete should return the deleted stasjon
         */
        @Test
        fun `delete stasjon 200`() {
            val expected = Stasjon(1, "Test")
            every { StasjonService.deleteStasjonById(1) } returns expected.right()

            testDelete("/stasjoner/1") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Stasjon.serializer(), expected), response.content)
            }
        }

        /*
        Attempting to delete a non-existing stasjon should return a 404
         */
        @Test
        fun `delete stasjon 404`() {
            every { StasjonService.deleteStasjonById(1) } returns RepositoryError.NoRowsFound("1").left()

            testDelete("/stasjoner/1") {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals("No rows found with provided ID, 1", response.content)
            }
        }

        /*
        Path parameter value for delete should be greater than 0
         */
        @Test
        fun `delete stasjon 422`() {
            testDelete("/stasjoner/0") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }

        /*
        Path parameter value that cannot be parsed to int should return 400
         */
        @Test
        fun `delete stasjon bad path`() {
            testDelete("/stasjoner/asdasd") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }

        /*
        Check for 500 when we encounter a serious error
         */
        @Test
        fun `delete stasjon 500`() {
            every { StasjonService.deleteStasjonById(1) } returns ServiceError("test").left()

            testDelete("/stasjoner/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }

        /*
        Partnere should not be allowed to delete a stasjon
         */
        @Test
        fun `delete stasjon 403`() {
            testDelete("stasjoner/1", JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /*
        Stasjon workers should not be allowed to delete a stasjon
         */
        @Test
        fun `delete stasjon stasjon worker 403`() {
            testDelete("/stasjoner/1", JwtMockConfig.reuseStasjonBearer) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /*
        Cannot delete stasjon without being authenticated
         */
        @Test
        fun `delete stasjon without bearer 401`() {
            testDelete("/stasjoner/1", null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }


    }
}
 */