package pickup.form

import arrow.core.Either
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.pickup.form.pickup.PickupPostForm
import ombruk.backend.shared.database.initDB
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class PickupPostFormTest {

    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        mockkObject(StationRepository)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Suppress("unused")
    fun generateValidForms() = listOf(
        PickupPostForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1), "desc", 1),
        PickupPostForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1), stationId = 1)
    )
    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: PickupPostForm) {
        every { StationRepository.exists(1) } returns true

        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        PickupPostForm(LocalDateTime.now(), LocalDateTime.now().minusHours(1), "desc", 1),
        PickupPostForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1), "", 1) ,
        PickupPostForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1), "desc", 0),
        PickupPostForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1), "desc", 2)
    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: PickupPostForm) {
        every { StationRepository.exists(1) } returns true

        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}