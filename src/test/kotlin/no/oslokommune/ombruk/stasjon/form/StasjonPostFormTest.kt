package no.oslokommune.ombruk.stasjon.form

import arrow.core.Either
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.shared.database.initDB
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class StasjonPostFormTest {

    @BeforeEach
    fun setup() {
        mockkObject(StasjonRepository)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @AfterAll
    fun finish() {
        unmockkAll()
    }

    @Suppress("unused")
    fun generateValidForms() = listOf(
        StasjonPostForm("unique", emptyMap()),
        StasjonPostForm("unique", mapOf(
            Pair(DayOfWeek.MONDAY, listOf(
                LocalTime.parse("09:00", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("20:00", DateTimeFormatter.ISO_TIME)
            ))
        ))
    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: StasjonPostForm) {
        every {StasjonRepository.exists(name = form.navn)} returns false
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        StasjonPostForm("", emptyMap()),
        StasjonPostForm("notUnique", emptyMap()),
        StasjonPostForm("", mapOf(DayOfWeek.MONDAY to emptyList()))
    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: StasjonPostForm) {
        every { StasjonRepository.exists(name = "") } returns false
        every { StasjonRepository.exists("notUnique") } returns true

        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}