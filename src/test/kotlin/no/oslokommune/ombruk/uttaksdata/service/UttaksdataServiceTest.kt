package no.oslokommune.ombruk.uttaksdata.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import no.oslokommune.ombruk.uttaksdata.database.UttaksdataRepository
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.Uttaksdata
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataPostForm
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class UttaksdataServiceTest {

    @BeforeEach
    fun setup() {
        mockkObject(UttaksdataRepository)
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
    inner class GetUttaksdataTable {

        /**
         * Get uttaksdata by ID should return the expected no.oslokommune.ombruk.pickup
         */
        @Test
        fun `get uttaksdata by id that exists`(@MockK expected: Uttaksdata) {
            val id = 1
            every { UttaksdataRepository.getUttaksDataByID(id) } returns expected.right()

            val actualReport = UttaksdataService.getUttaksdataById(id)
            require(actualReport is Either.Right)
            assertEquals(expected, actualReport.b)
        }

        /**
         * Get uttaksdata by non-existing ID should return a RepositoryError.NoRowsFound error
         */
        @Test
        fun `get uttaksdata by id that does not exist not found`() {
            val id = 1
            val expected = RepositoryError.NoRowsFound("test")
            every { UttaksdataRepository.getUttaksDataByID(id) } returns expected.left()

            val result = UttaksdataService.getUttaksdataById(id)
            require(result is Either.Left)
            assertEquals(expected, result.a)
        }

        /**
         * If get uttaksdata by id fails, it should return a RepositoryError.SelectError
         */
        @Test
        fun `get uttaksdata by id fails`() {
            val id = 1
            val expected = RepositoryError.SelectError("test")
            every { UttaksdataRepository.getUttaksDataByID(id) } returns expected.left()

            val actual = UttaksdataService.getUttaksdataById(id)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        /**
         * Get all uttaksdata returns right if valid
         */
        @Test
        fun `get all uttaksdata valid`(@MockK expectedUttaksdata: List<Uttaksdata>) {
            every { UttaksdataRepository.getUttaksData(UttaksdataGetForm()) } returns expectedUttaksdata.right()

            val actualReports = UttaksdataService.getUttaksdata(UttaksdataGetForm())
            require(actualReports is Either.Right)
            assertEquals(expectedUttaksdata, actualReports.b)
        }

        /**
         * If get all uttaksdata fails, a left should be returned
         */
        @Test
        fun `get all uttaksdata fails`() {
            val expected = RepositoryError.SelectError("test")
            every { UttaksdataRepository.getUttaksData(UttaksdataGetForm()) } returns expected.left()
            val actual = UttaksdataService.getUttaksdata(UttaksdataGetForm())
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
        fun `Save uttaksdata success`(@MockK uttaksdataPostForm: UttaksdataPostForm, @MockK expected: Uttaksdata) {
            every { UttaksdataRepository.insertUttaksdata(uttaksdataPostForm) } returns expected.right()

            val actual = UttaksdataService.saveUttaksdata(uttaksdataPostForm)
            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

    }

    @Nested
    inner class Update {

        /**
         * A successful uttaksdata with a ReportUpdateForm should return the updated uttaksdata
         */
        @Test
        fun `update uttaksdata with form success`(@MockK form: UttaksdataUpdateForm, @MockK expected: Uttaksdata) {
            every { UttaksdataRepository.updateUttaksdata(form) } returns expected.right()

            val actual = UttaksdataService.updateUttaksdata(form)
            require(actual is Either.Right)
            assertEquals(expected, actual.b)

        }

        /**
         * A failed uttaksdata update with a ReportUpdateForm should return a RepositoryError.UpdateError
         */
        @Test
        fun `update uttaksdata with form failure`(@MockK form: UttaksdataUpdateForm) {
            val expected = RepositoryError.UpdateError("test")
            every { UttaksdataRepository.updateUttaksdata(form) } returns expected.left()

            val actual = UttaksdataService.updateUttaksdata(form)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

}