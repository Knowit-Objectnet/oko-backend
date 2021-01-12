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
import no.oslokommune.ombruk.uttaksdata.database.UttaksDataRepository
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.UttaksData
import no.oslokommune.ombruk.shared.error.RepositoryError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class UttaksDataServiceTest {

    @BeforeEach
    fun setup() {
        mockkObject(UttaksDataRepository)
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
    inner class GetUttaksDataTable {

        /**
         * Get uttaksdata by ID should return the expected [UttaksData]
         */
        @Test
        fun `get uttaksdata by id that exists`(@MockK expected: UttaksData) {
            val id = 1
            every { UttaksDataRepository.getUttaksDataById(id) } returns expected.right()

            val actualReport = UttaksDataService.getUttaksDataById(id)
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
            every { UttaksDataRepository.getUttaksDataById(id) } returns expected.left()

            val result = UttaksDataService.getUttaksDataById(id)
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
            every { UttaksDataRepository.getUttaksDataById(id) } returns expected.left()

            val actual = UttaksDataService.getUttaksDataById(id)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        /**
         * Get all uttaksdata returns right if valid
         */
        @Test
        fun `get all uttaksdata valid`(@MockK expectedUttaksdata: List<UttaksData>) {
            every { UttaksDataRepository.getUttaksData(UttaksDataGetForm()) } returns expectedUttaksdata.right()

            val actualReports = UttaksDataService.getUttaksData(UttaksDataGetForm())
            require(actualReports is Either.Right)
            assertEquals(expectedUttaksdata, actualReports.b)
        }

        /**
         * If get all uttaksdata fails, a left should be returned
         */
        @Test
        fun `get all uttaksdata fails`() {
            val expected = RepositoryError.SelectError("test")
            every { UttaksDataRepository.getUttaksData(UttaksDataGetForm()) } returns expected.left()
            val actual = UttaksDataService.getUttaksData(UttaksDataGetForm())
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

    }

    @Nested
    inner class Update {

        /**
         * A successful uttaksdata with a ReportUpdateForm should return the updated uttaksdata
         */
        @Test
        fun `update uttaksdata with form success`(@MockK form: UttaksDataUpdateForm, @MockK expected: UttaksData) {
            every { UttaksDataRepository.updateUttaksData(form) } returns expected.right()

            val actual = UttaksDataService.updateUttaksData(form)
            require(actual is Either.Right)
            assertEquals(expected, actual.b)

        }

        /**
         * A failed uttaksdata update with a ReportUpdateForm should return a RepositoryError.UpdateError
         */
        @Test
        fun `update uttaksdata with form failure`(@MockK form: UttaksDataUpdateForm) {
            val expected = RepositoryError.UpdateError("test")
            every { UttaksDataRepository.updateUttaksData(form) } returns expected.left()

            val actual = UttaksDataService.updateUttaksData(form)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

}