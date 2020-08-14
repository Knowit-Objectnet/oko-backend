package pickup.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import ombruk.backend.pickup.database.RequestRepository
import ombruk.backend.pickup.form.request.RequestDeleteForm
import ombruk.backend.pickup.form.request.RequestGetForm
import ombruk.backend.pickup.form.request.RequestPostForm
import ombruk.backend.pickup.model.Request
import ombruk.backend.pickup.service.RequestService
import ombruk.backend.shared.error.RepositoryError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class RequestServiceTest {


    @BeforeEach
    fun setup() {
        mockkObject(RequestRepository)
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
        fun `get requests success`(@MockK expected: List<Request>, @MockK form: RequestGetForm) {

            every { RequestRepository.getRequests(form) } returns expected.right()

            val actual = RequestService.getRequests(form)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `get requests repository error`(@MockK expected: RepositoryError.NoRowsFound, @MockK form: RequestGetForm) {

            every { RequestRepository.getRequests(form) } returns expected.left()

            val actual = RequestService.getRequests(form)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

    }

    @Nested
    inner class SaveRequests {
        @Test
        fun `save requests success`(@MockK expected: Request, @MockK form: RequestPostForm) {

            every { RequestRepository.saveRequest(form) } returns expected.right()

            val actual = RequestService.saveRequest(form)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `save requests repository error`(@MockK expected: RepositoryError.InsertError, @MockK form: RequestPostForm) {

            every { RequestRepository.saveRequest(form) } returns expected.left()

            val actual = RequestService.saveRequest(form)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

    }

    @Nested
    inner class DeleteRequests {
        @Test
        fun `delete requests success`(@MockK expected: Request, @MockK form: RequestDeleteForm) {

            every { RequestRepository.deleteRequest(form) } returns 1.right()


            val actual = RequestService.deleteRequest(form)

            require(actual is Either.Right)
            assertEquals(1, actual.b)
        }

        @Test
        fun `delete requests repository error`(@MockK expected: RepositoryError.DeleteError, @MockK form: RequestDeleteForm) {

            every { RequestRepository.deleteRequest(form) } returns expected.left()

            val actual = RequestService.deleteRequest(form)

            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

    }

}