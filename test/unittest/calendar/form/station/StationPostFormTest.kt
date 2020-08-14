package calendar.form.station

import arrow.core.Either
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.calendar.form.station.StationPostForm
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
class StationPostFormTest {

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
        StationPostForm("unique", emptyMap()),
        StationPostForm("unique")
    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: StationPostForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        StationPostForm(""),
        StationPostForm("notUnique"),
        StationPostForm("", mapOf(DayOfWeek.MONDAY to emptyList()))
        )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: StationPostForm) {
        every { StationRepository.exists("notUnique") } returns true

        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}