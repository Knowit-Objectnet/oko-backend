package no.oslokommune.ombruk.uttaksdata.api

import arrow.core.left
import arrow.core.right
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import no.oslokommune.ombruk.shared.api.Authorization
import no.oslokommune.ombruk.shared.api.Authorization.authorizeRole
import no.oslokommune.ombruk.shared.api.JwtMockConfig
import no.oslokommune.ombruk.shared.api.Roles
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.testGet
import no.oslokommune.ombruk.testPatch
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttak.service.UttakService
import no.oslokommune.ombruk.uttaksdata.database.UttaksDataRepository
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.UttaksData
import no.oslokommune.ombruk.uttaksdata.service.UttaksDataService
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class UttaksDataApiTest {

    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    @BeforeEach
    fun setup() {
        mockkObject(UttaksDataService)
        mockkObject(UttakService)
        mockkObject(UttakRepository)
        mockkObject(Authorization)
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
        fun `get uttaksdata by id 200`() {
            val expected = UttaksData(
                1,
                1,
                LocalDateTime.parse("2020-07-07T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME)
            )

            every { UttaksDataService.getUttaksDataById(1) } returns expected.right()

            testGet("/uttaksdata/1") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(UttaksData.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 404 when getting non-existing ID
         */
        @Test
        fun `get uttaksdata by id 404`() {
            every { UttaksDataService.getUttaksDataById(1) } returns RepositoryError.NoRowsFound("1").left()

            testGet("/uttaksdata/1") {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals("No rows found with provided ID, 1", response.content)
            }
        }

        /**
         * Check for 500 when encountering a serious error.
         */
        @Test
        fun `get uttaksdata by id 500`() {
            every { UttaksDataService.getUttaksDataById(1) } returns ServiceError("test").left()

            testGet("/uttaksdata/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }

        /**
         * Check for 400 when input cannot be parsed to int
         */
        @Test
        fun `get uttaksdata by bad id 400`() {

            testGet("/uttaksdata/asdasd") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }

        /**
         * Check for 422 when ID is invalid (ID < 1)
         */
        @Test
        fun `get uttaksdata by invalid 422`() {

            testGet("/uttaksdata/-1") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
            }
        }
    }

    @Nested
    inner class Get {

        /**
         * Check for 200 when getting all
         */
        @Test
        fun `get all uttaksdata 200`() {
            val r1 = UttaksData(
                1,
                1,
                LocalDateTime.parse("2020-07-07T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            val r2 = r1.copy(uttakId = 2)
            val r3 = r1.copy(uttakId = 3)
            val expected = listOf(r1, r2, r3)

            every { UttaksDataService.getUttaksData(any()) } returns expected.right()

            testGet("/uttaksdata/") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(UttaksData.serializer().list, expected), response.content)
            }
        }

        /**
         * Check for 200 when getting uttaksdata by valid stasjonID
         */
        @Test
        fun `get all uttaksdata by stasjonID 200`() {
            val r1 = UttaksData(
                1,
                1,
                LocalDateTime.parse("2020-07-07T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            val r3 = r1.copy(uttakId = 3)
            val expected = listOf(r1, r3)
            val form = UttaksDataGetForm(stasjonId = 1)

            every { UttaksDataService.getUttaksData(form) } returns expected.right()

            testGet("/uttaksdata/?stasjonId=1") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(UttaksData.serializer().list, expected), response.content)
            }
        }

        /**
         * Check for 500 when we encounter a serious error
         */
        @Test
        fun `get uttaksdata 500`() {
            every { UttaksDataService.getUttaksData(UttaksDataGetForm()) } returns ServiceError("test").left()
            testGet("/uttaksdata/") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which cannot be parsed. fromDate is not valid
         */
        @Test
        fun `get uttaksdata invalid fromDate 400`() {
            testGet("/uttaksdata/?fraRapportertTidspunkt=2020-07-0912:13:15Z") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("fraRapportertTidspunkt could not be parsed.", response.content)
            }
        }

        /**
         * Check for 422 when we get an invalid form. fromDate is greater than toDate
         */
        @Test
        fun `get uttaksdata fromDate larger than endDate 422`() {
            testGet("/uttaksdata/?fraRapportertTidspunkt=2020-07-07T12:13:15Z&tilRapportertTidspunkt=2020-06-06T12:13:15Z") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("fraRapportertTidspunkt: has to be less than end datetime", response.content)
            }
        }
    }

    @Nested
    inner class Patch {

        /**
         * Patch returns 200 when getting valid form
         */
        @Test
        fun `patch uttaksdata 200`(@MockK uttak: Uttak) {
            val initial = UttaksData(
                1,
                1,
                LocalDateTime.parse("2020-07-07T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            val form = UttaksDataUpdateForm(1, 50)
            val expected = initial.copy(vekt = 50)

            every { UttaksDataService.updateUttaksData(form) } returns expected.right()
            every { UttakService.getUttakByID(form.uttakId) } returns uttak.right()
            every { UttakRepository.exists(1) } returns true

            testPatch("/uttaksdata/", json.stringify(UttaksDataUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(UttaksData.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present
         */

        @Test
        fun `patch uttaksdata no bearer 401`() {
            val form = UttaksDataUpdateForm(1, 50)
            testPatch("/uttaksdata/", json.stringify(UttaksDataUpdateForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when a partner attempts to edit another partner's uttaksdata
         */
        @Test
        fun `patch uttaksdata of other partner 403`(@MockK uttak: Uttak) {
            val initial = UttaksData(
                1,
                1,
                LocalDateTime.parse("2020-07-07T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME)
            )

            every { uttak.partner!!.id } answers { 1 }
            val form = UttaksDataUpdateForm(1, 50)

            every { UttaksDataService.getUttaksDataById(1) } returns initial.right()
            every { UttakService.getUttakByID(any()) } returns uttak.right()

            testPatch(
                "/uttaksdata/",
                json.stringify(UttaksDataUpdateForm.serializer(), form),
                JwtMockConfig.partnerBearer2
            ) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /**
         * Check for 200 when a partner edits his own uttaksdata
         */
        @Test
        fun `patch own uttaksdata partner 200`(@MockK uttak: Uttak) {
            val initial = UttaksData(
                1,
                1,
                LocalDateTime.parse("2020-07-07T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            val form = UttaksDataUpdateForm(1, 50)
            val expected = initial.copy(vekt = 50)

            every { UttaksDataService.getUttaksDataById(1) } returns initial.right()
            every { UttakService.getUttakByID(any()) } returns uttak.right()
            every { uttak.partner!!.id } answers { 1 }
            every { UttaksDataService.updateUttaksData(form) } returns expected.right()

            testPatch(
                "/uttaksdata/",
                json.stringify(UttaksDataUpdateForm.serializer(), form),
                JwtMockConfig.partnerBearer1
            ) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(UttaksData.serializer(), expected), response.content)
            }
        }

        /*
        A uttaksdata with weight less than 1 should be unprocessable
         */
        @Test
        fun `patch uttaksdata invalid weight 422`() {
            val form = UttaksDataUpdateForm(1, 0)

            testPatch("/uttaksdata/", json.stringify(UttaksDataUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("vekt: Must be greater than 0", response.content)
            }
        }

        /*
        Test 404 when uttaksdata does not exist
         */
        @Test
        fun `patch uttaksdata not found 404`() {
            val form = UttaksDataUpdateForm(1, 50)
            val uttak = mockk<Uttak>()
            val expected = RepositoryError.NoRowsFound("${form.uttakId} does not exist!")
            every { UttaksDataService.updateUttaksData(form) } returns expected.left()
            every { UttakService.getUttakByID(form.uttakId) } returns uttak.right()
            testPatch("/uttaksdata/", json.stringify(UttaksDataUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals("No rows found with provided ID, ${form.uttakId} does not exist!", response.content)
            }
        }

        /**
         * Check for 400 when we get a form that cannot be parsed.
         */
        @Test
        fun `patch uttaksdata invalid body 400`() {
            val testJson = """ {"id": 1, "weight": "asdf"} """
            testPatch("/uttaksdata/", testJson) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }

        /**
         * Check for 500 when we encounter a serious error
         */

        @Test
        fun `patch uttaksdata 500`(@MockK uttak: Uttak) {
            val test = UttaksData(
                1,
                1,
                LocalDateTime.parse("2020-07-07T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            val form = UttaksDataUpdateForm(1, 50)
            every { UttaksDataService.updateUttaksData(form) } returns ServiceError("test").left()
            every { UttaksDataService.getUttaksDataById(1) } returns test.right()
            every { UttakService.getUttakByID(any()) } returns uttak.right()
            every { UttakRepository.exists(1) } returns true

            testPatch("/uttaksdata/", json.stringify(UttaksDataUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }
    }

}