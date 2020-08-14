package calendar.form.station

import arrow.core.Either
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.calendar.form.station.StationUpdateForm
import ombruk.backend.shared.database.initDB
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.DayOfWeek
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class StationUpdateFormTest {

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

    @AfterAll
    fun finish(){
        unmockkAll()
    }

    @Suppress("unused")
    fun generateValidForms() = listOf(
        StationUpdateForm(1, "unique"),
        StationUpdateForm(1, "unique", emptyMap()),
        StationUpdateForm(1, hours = emptyMap())
    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: StationUpdateForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        StationUpdateForm(0),
        StationUpdateForm(1, ""),
        StationUpdateForm(1, "notUnique"),
        StationUpdateForm(1, hours = mapOf(DayOfWeek.MONDAY to emptyList()))
        )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: StationUpdateForm) {
        every { StationRepository.exists("notUnique") } returns true

        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}