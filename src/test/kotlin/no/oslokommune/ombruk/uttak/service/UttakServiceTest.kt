package no.oslokommune.ombruk.uttak.service

import arrow.core.Either
import arrow.core.right
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.uttak.database.RecurrenceRules
import no.oslokommune.ombruk.uttak.form.UttakDeleteForm
import no.oslokommune.ombruk.uttak.form.UttakGetForm
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.form.UttakUpdateForm
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttak.model.RecurrenceRule
import no.oslokommune.ombruk.reporting.model.Report
import no.oslokommune.ombruk.reporting.service.ReportService
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class UttakServiceTest {

    @BeforeEach
    fun setup() {
        mockkObject(UttakRepository)
        mockkObject(ReportService)
        mockkObject(RecurrenceRules)
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
    inner class GetUttakTable {
        /**
         * Check that get by id returns the expected uttak.
         */
        @Test
        fun `get by id`(@MockK expectedUttak: Uttak) {
            val id = 1
            every { UttakRepository.getUttakByID(id) } returns expectedUttak.right()

            val actualUttak = UttakService.getUttakByID(id)
            require(actualUttak is Either.Right)

            assertEquals(expectedUttak, actualUttak.b)
        }

        /**
         * Check that get uttaks returns the exepected list of uttaks
         */
        @Test
        fun `get all`(@MockK expectedUttaks: List<Uttak>) {
            every { UttakRepository.getUttaks(null) } returns expectedUttaks.right()

            val actualUttaks = UttakService.getUttaks()
            require(actualUttaks is Either.Right)

            assertEquals(expectedUttaks, actualUttaks.b)
        }

        /**
         * Check that we can get the expected uttak when given a stasjon id
         */
        @Test
        fun `get by stasjon id`(@MockK expectedUttaks: List<Uttak>) {
            val form = UttakGetForm(stasjonId = 1)
            every { UttakRepository.getUttaks(form) } returns expectedUttaks.right()

            val actualUttaks = UttakService.getUttaks(form)
            require(actualUttaks is Either.Right)
            assertEquals(expectedUttaks, actualUttaks.b)
        }

        /**
         * Check that we can get the expected uttak when given a partner id
         */
        @Test
        fun `get by partner id`(@MockK expectedUttaks: List<Uttak>) {
            val form = UttakGetForm(partnerId = 1)
            every { UttakRepository.getUttaks(form) } returns expectedUttaks.right()

            val actualUttaks = UttakService.getUttaks(form)
            require(actualUttaks is Either.Right)
            assertEquals(expectedUttaks, actualUttaks.b)
        }

        /**
         * Check that we can get the expected uttak when given a stasjon and partner id
         */
        @Test
        fun `get by partner and stasjon id`(@MockK expectedUttaks: List<Uttak>) {
            val form = UttakGetForm(partnerId = 1, stasjonId = 1)
            every { UttakRepository.getUttaks(form) } returns expectedUttaks.right()

            val actualUttaks = UttakService.getUttaks(form)
            require(actualUttaks is Either.Right)
            assertEquals(expectedUttaks, actualUttaks.b)
        }

        /**
         * Check that we can get the expected uttak when given a date time range
         */
        @Test
        fun `get by datetime range`(@MockK expectedUttaks: List<Uttak>) {
            val form = UttakGetForm(
                fromDate = LocalDateTime.parse("2020-08-15T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                toDate = LocalDateTime.parse("2020-08-20T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
            )
            every { UttakRepository.getUttaks(form) } returns expectedUttaks.right()

            val actualUttaks = UttakService.getUttaks(form)
            require(actualUttaks is Either.Right)
            assertEquals(expectedUttaks, actualUttaks.b)
        }

        /**
         * Check that we can get the expected uttak when given a recurrence rule id
         */
        @Test
        fun `get by recurrenceRule id`(@MockK expectedUttaks: List<Uttak>) {
            val form = UttakGetForm(recurrenceRuleId = 1)
            every { UttakRepository.getUttaks(form) } returns expectedUttaks.right()

            val actualUttaks = UttakService.getUttaks(form)
            require(actualUttaks is Either.Right)
            assertEquals(expectedUttaks, actualUttaks.b)
        }

    }

    @Nested
    inner class SaveUttakTable {

        /**
         * Check that save single uttaks calls the repository and returns the saved uttak.
         */
        @Test
        fun `save single uttak`(@MockK expectedUttak: Uttak, @MockK report: Report) {
            val from = LocalDateTime.parse("2020-09-02T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
            val form = UttakPostForm(from, from.plusHours(1), 1, 1)
            every { UttakRepository.insertUttak(form) } returns expectedUttak.right()
            every { ReportService.saveReport(expectedUttak) } returns report.right()

            val actualUttak = UttakService.saveUttak(form)
            require(actualUttak is Either.Right)
            verify(exactly = 1) { UttakRepository.insertUttak(form) }
            assertEquals(expectedUttak, actualUttak.b)
        }

        /**
         * Check that the repository is called 3 times, because 3 uttaks should be saved.
         */
        @Test
        fun `save recurring uttak`(@MockK expectedUttak: Uttak, @MockK report: Report) {
            val rRule = RecurrenceRule(count = 3, days = listOf(DayOfWeek.MONDAY))
            val from = LocalDateTime.parse("2020-09-02T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
            val form = UttakPostForm(from, from.plusHours(1), 1, 1, recurrenceRule = rRule)

            every { RecurrenceRules.insertRecurrenceRule(rRule) } returns rRule.right()
            every { ReportService.saveReport(expectedUttak) } returns report.right()
            every { UttakRepository.insertUttak(any()) } returns expectedUttak.right()

            val actualUttak = UttakService.saveUttak(form)
            require(actualUttak is Either.Right)
            verify(exactly = 3) { UttakRepository.insertUttak(any()) }
        }

    }

    @Nested
    inner class UpdateUttakTable {

        /**
         * Check that update uttak returns the updated uttak.
         */
        @Test
        fun `update single uttak`(@MockK expectedUttak: Uttak) {
            val from = LocalDateTime.parse("2020-09-02T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
            val updateForm = UttakUpdateForm(1, from, from.plusHours(1))

            every { ReportService.updateReport(expectedUttak) } returns Unit.right()
            every { UttakRepository.updateUttak(updateForm) } returns expectedUttak.right()

            val actualUttak = UttakService.updateUttak(updateForm)

            require(actualUttak is Either.Right)
            assertEquals(expectedUttak, actualUttak.b)
        }
    }

    @Nested
    inner class DeleteUttakTable {

        /**
         * Delete uttak doesn't really have any logic so we just have to check if it actually calls
         * the repository.
         */
        @Test
        fun `delete uttak by id`() {
            val deleteForm = UttakDeleteForm(1)

            UttakService.deleteUttak(deleteForm)
            verify(exactly = 1) { UttakRepository.deleteUttak(deleteForm) }
        }

    }
}