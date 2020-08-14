package reporting.api

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
import ombruk.backend.calendar.model.Station
import ombruk.backend.reporting.database.ReportRepository
import ombruk.backend.reporting.form.ReportGetForm
import ombruk.backend.reporting.form.ReportUpdateForm
import ombruk.backend.reporting.model.Report
import ombruk.backend.reporting.service.ReportService
import ombruk.backend.shared.api.JwtMockConfig
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.shared.error.ServiceError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import testutils.testGet
import testutils.testPatch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class ReportingApiTest {

    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    @BeforeEach
    fun setup() {
        mockkObject(ReportService)
        mockkObject(ReportRepository)
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
        fun `get report by id 200`() {
            val expected = Report(
                1,
                1,
                1,
                Station(1, "Test"),
                LocalDateTime.parse("2020-07-07T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-08T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME)
            )

            every { ReportService.getReportById(1) } returns expected.right()

            testGet("/reports/1") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Report.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 404 when getting non-existing ID
         */
        @Test
        fun `get report by id 404`() {
            every { ReportService.getReportById(1) } returns RepositoryError.NoRowsFound("1").left()

            testGet("/reports/1") {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals("No rows found with provided ID, 1", response.content)
            }
        }

        /**
         * Check for 500 when encountering a serious error.
         */
        @Test
        fun `get report by id 500`() {
            every { ReportService.getReportById(1) } returns ServiceError("test").left()

            testGet("/reports/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }

        /**
         * Check for 400 when input cannot be parsed to int
         */
        @Test
        fun `get report by bad id 400`() {

            testGet("/reports/asdasd") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }

        /**
         * Check for 422 when ID is invalid (ID < 1)
         */
        @Test
        fun `get report by invalid 422`() {

            testGet("/reports/-1") {
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
        fun `get all reports 200`() {
            val r1 = Report(
                1,
                1,
                1,
                Station(1, "Test"),
                LocalDateTime.parse("2020-07-07T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-08T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            val r2 = r1.copy(reportId = 2)
            val r3 = r1.copy(reportId = 3)
            val expected = listOf(r1, r2, r3)

            every { ReportService.getReports(any()) } returns expected.right()

            testGet("/reports/") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Report.serializer().list, expected), response.content)
            }
        }

        /**
         * Check for 200 when getting reports by valid stationID
         */
        @Test
        fun `get all reports by stationID 200`() {
            val r1 = Report(
                1,
                1,
                1,
                Station(1, "Test"),
                LocalDateTime.parse("2020-07-07T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-08T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            val r3 = r1.copy(reportId = 3)
            val expected = listOf(r1, r3)
            val form = ReportGetForm(stationId = 1)

            every { ReportService.getReports(form) } returns expected.right()

            testGet("/reports/?stationId=1") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Report.serializer().list, expected), response.content)
            }
        }

        /**
         * Check for 500 when we encounter a serious error
         */
        @Test
        fun `get reports 500`() {
            every { ReportService.getReports(ReportGetForm()) } returns ServiceError("test").left()
            testGet("/reports/") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which cannot be parsed. fromDate is not valid
         */
        @Test
        fun `get reports invalid fromDate 400`() {
            testGet("/reports/?fromDate=2020-07-0912:13:15Z") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("fromDate could not be parsed.", response.content)
            }
        }

        /**
         * Check for 422 when we get an invalid form. fromDate is greater than toDate
         */
        @Test
        fun `get reports fromDate larger than endDate 422`() {
            testGet("/reports/?fromDate=2020-07-07T12:13:15Z&toDate=2020-06-06T12:13:15Z") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("fromDate: has to be less than end datetime", response.content)
            }
        }
    }

    @Nested
    inner class Patch {

        /**
         * Patch returns 200 when getting valid form
         */
        @Test
        fun `patch report 200`() {
            val initial = Report(
                1,
                1,
                1,
                Station(1, "Test"),
                LocalDateTime.parse("2020-07-07T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-08T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            val form = ReportUpdateForm(1, 50)
            val expected = initial.copy(weight = 50)

            every { ReportService.getReportById(1) } returns initial.right()
            every { ReportService.updateReport(form) } returns expected.right()

            testPatch("/reports/", json.stringify(ReportUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Report.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 401 when no bearer is present
         */

        @Test
        fun `patch report no bearer 401`() {
            val form = ReportUpdateForm(1, 50)
            testPatch("/reports/", json.stringify(ReportUpdateForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when a partner attempts to edit another partner's report
         */
        @Test
        fun `patch report of other partner 403`() {
            val initial = Report(
                1,
                1,
                1,
                Station(1, "Test"),
                LocalDateTime.parse("2020-07-07T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-08T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            val form = ReportUpdateForm(1, 50)

            every { ReportService.getReportById(1) } returns initial.right()

            testPatch("/reports/", json.stringify(ReportUpdateForm.serializer(), form), JwtMockConfig.partnerBearer2) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /**
         * Check for 200 when a partner edits his own report
         */
        @Test
        fun `patch own report partner 200`() {
            val initial = Report(
                1,
                1,
                1,
                Station(1, "Test"),
                LocalDateTime.parse("2020-07-07T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-08T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            val form = ReportUpdateForm(1, 50)
            val expected = initial.copy(weight = 50)

            every { ReportService.getReportById(1) } returns initial.right()
            every { ReportService.updateReport(form) } returns expected.right()

            testPatch("/reports/", json.stringify(ReportUpdateForm.serializer(), form), JwtMockConfig.partnerBearer1) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Report.serializer(), expected), response.content)
            }
        }

        /*
        A report with weight less than 1 should be unprocessable
         */
        @Test
        fun `patch report invalid weight 422`() {
            val form = ReportUpdateForm(1, 0)

            testPatch("/reports/", json.stringify(ReportUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("weight: Must be greater than 0", response.content)
            }
        }

        /*
        Test 404 when report does not exist
         */
        @Test
        fun `patch report not found 404`() {
            val form = ReportUpdateForm(1, 50)
            every { ReportService.updateReport(form) } returns RepositoryError.NoRowsFound("test").left()
            testPatch("/reports/", json.stringify(ReportUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals("No rows found with provided ID, ID 1 does not exist!", response.content)
            }
        }

        /**
         * Check for 400 when we get a form that cannot be parsed.
         */
        @Test
        fun `patch report invalid body 400`() {
            val testJson = """ {"id": 1, "weight": "asdf"} """
            testPatch("/reports/", testJson) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }

        /**
         * Check for 500 when we encounter a serious error
         */

        @Test
        fun `patch report 500`() {
            val test = Report(
                1,
                1,
                1,
                Station(1, "Test"),
                LocalDateTime.parse("2020-07-07T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-08T15:15:15Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            val form = ReportUpdateForm(1, 50)
            every { ReportService.updateReport(form) } returns ServiceError("test").left()
            every { ReportService.getReportById(1) } returns test.right()

            testPatch("/reports/", json.stringify(ReportUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }
    }
}