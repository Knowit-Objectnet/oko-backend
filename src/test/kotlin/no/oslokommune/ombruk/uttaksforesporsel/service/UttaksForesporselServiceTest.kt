package no.oslokommune.ombruk.uttaksforesporsel.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import no.oslokommune.ombruk.uttaksforesporsel.database.UttaksforesporselRepository
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselDeleteForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksForesporselGetForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselPostForm
import no.oslokommune.ombruk.uttaksforesporsel.model.UttaksForesporsel
import no.oslokommune.ombruk.shared.error.RepositoryError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class UttaksForesporselServiceTest {


    @BeforeEach
    fun setup() {
        mockkObject(UttaksforesporselRepository)
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
    inner class GetRequests {
        @Test
        fun `get requests success`(@MockK expected: List<UttaksForesporsel>, @MockK form: UttaksForesporselGetForm) {

            every { UttaksforesporselRepository.getForesporsler(form) } returns expected.right()

            val actual = UttaksforesporselService.getForesporsler(form)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `get requests repository error`(@MockK expected: RepositoryError.NoRowsFound, @MockK form: UttaksForesporselGetForm) {

            every { UttaksforesporselRepository.getForesporsler(form) } returns expected.left()

            val actual = UttaksforesporselService.getForesporsler(form)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

    }

    @Nested
    inner class SaveRequests {
        @Test
        fun `save requests success`(@MockK expected: UttaksForesporsel, @MockK form: UttaksforesporselPostForm) {

            every { UttaksforesporselRepository.saveForesporsel(form) } returns expected.right()

            val actual = UttaksforesporselService.saveForesporsel(form)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `save requests repository error`(
            @MockK expected: RepositoryError.InsertError,
            @MockK form: UttaksforesporselPostForm
        ) {

            every { UttaksforesporselRepository.saveForesporsel(form) } returns expected.left()

            val actual = UttaksforesporselService.saveForesporsel(form)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

    }

    @Nested
    inner class DeleteRequests {
        @Test
        fun `delete requests success`(@MockK expected: UttaksForesporsel, @MockK form: UttaksforesporselDeleteForm) {

            every { UttaksforesporselRepository.deleteForesporsel(form) } returns 1.right()


            val actual = UttaksforesporselService.deleteForesporsel(form)

            require(actual is Either.Right)
            assertEquals(1, actual.b)
        }

        @Test
        fun `delete requests repository error`(
            @MockK expected: RepositoryError.DeleteError,
            @MockK form: UttaksforesporselDeleteForm
        ) {

            every { UttaksforesporselRepository.deleteForesporsel(form) } returns expected.left()

            val actual = UttaksforesporselService.deleteForesporsel(form)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

    }

}