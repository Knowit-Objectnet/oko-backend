package no.oslokommune.ombruk.reporting.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.reporting.database.ReportRepository
import no.oslokommune.ombruk.reporting.form.ReportGetForm
import no.oslokommune.ombruk.reporting.form.ReportUpdateForm
import no.oslokommune.ombruk.reporting.model.Report
import no.oslokommune.ombruk.shared.error.RepositoryError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class ReportServiceTest {

    @BeforeEach
    fun setup() {
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
    inner class GetReports {

        /**
         * Get report by ID should return the expected no.oslokommune.ombruk.pickup
         */
        @Test
        fun `get report by id that exists`(@MockK expected: Report) {
            val id = 1
            every { ReportRepository.getReportByID(id) } returns expected.right()

            val actualReport = ReportService.getReportById(id)
            require(actualReport is Either.Right)
            assertEquals(expected, actualReport.b)
        }

        /**
         * Get report by non-existing ID should return a RepositoryError.NoRowsFound error
         */
        @Test
        fun `get report by id that does not exist not found`() {
            val id = 1
            val expected = RepositoryError.NoRowsFound("test")
            every { ReportRepository.getReportByID(id) } returns expected.left()

            val result = ReportService.getReportById(id)
            require(result is Either.Left)
            assertEquals(expected, result.a)
        }

        /**
         * If get report by id fails, it should return a RepositoryError.SelectError
         */
        @Test
        fun `get report by id fails`() {
            val id = 1
            val expected = RepositoryError.SelectError("test")
            every { ReportRepository.getReportByID(id) } returns expected.left()

            val actual = ReportService.getReportById(id)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        /**
         * Get all reports returns right if valid
         */
        @Test
        fun `get all reports valid`(@MockK expectedReports: List<Report>) {
            every { ReportRepository.getReports(ReportGetForm()) } returns expectedReports.right()

            val actualReports = ReportService.getReports(ReportGetForm())
            require(actualReports is Either.Right)
            assertEquals(expectedReports, actualReports.b)
        }

        /**
         * If get all reports fails, a left should be returned
         */
        @Test
        fun `get all reports fails`() {
            val expected = RepositoryError.SelectError("test")
            every { ReportRepository.getReports(ReportGetForm()) } returns expected.left()
            val actual = ReportService.getReports(ReportGetForm())
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

    }

    @Nested
    inner class Insert {

        /**
         * A valid uttak should be processed successfully
         */
        @Test
        fun `Save report success`(@MockK uttak: Uttak, @MockK expected: Report) {
            every { ReportRepository.insertReport(uttak) } returns expected.right()

            val actual = ReportService.saveReport(uttak)
            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        /**
         * If inserting fails, a Left with a RepositoryError.InsertError should be returned
         */
        @Test
        fun `Save report failure`(@MockK uttak: Uttak) {
            val expected = RepositoryError.DeleteError("test")
            every { ReportRepository.insertReport(uttak) } returns expected.left()

            val actual = ReportService.saveReport(uttak)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

    @Nested
    inner class Update {

        /**
         * A successfull update should return a right
         */
        @Test
        fun `Update report successful`(@MockK uttak: Uttak) {
            every { ReportRepository.updateReport(uttak) } returns Unit.right()

            val actual = ReportService.updateReport(uttak)
            require(actual is Either.Right)
            assertEquals(Unit, actual.b)
        }

        /**
         * A failed update should return a RepositoryError.UpdateError
         */
        @Test
        fun `Update report failure`(@MockK uttak: Uttak) {
            val expected = RepositoryError.UpdateError("test")
            every { ReportRepository.updateReport(uttak) } returns expected.left()

            val actual = ReportService.updateReport(uttak)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        /**
         * A successful report with a ReportUpdateForm should return the updated report
         */
        @Test
        fun `update report with form success`(@MockK form: ReportUpdateForm, @MockK expected: Report) {
            every { ReportRepository.updateReport(form) } returns expected.right()

            val actual = ReportService.updateReport(form)
            require(actual is Either.Right)
            assertEquals(expected, actual.b)

        }

        /**
         * A failed report update with a ReportUpdateForm should return a RepositoryError.UpdateError
         */
        @Test
        fun `update report with form failure`(@MockK form: ReportUpdateForm) {
            val expected = RepositoryError.UpdateError("test")
            every { ReportRepository.updateReport(form) } returns expected.left()

            val actual = ReportService.updateReport(form)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

}