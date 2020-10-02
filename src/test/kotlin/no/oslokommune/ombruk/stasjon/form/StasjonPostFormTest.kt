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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class StasjonPostFormTest {

    init {
        initDB()
    }

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
        StasjonPostForm("unique")
    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: StasjonPostForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        StasjonPostForm(""),
        StasjonPostForm("notUnique"),
        StasjonPostForm("", mapOf(DayOfWeek.MONDAY to emptyList()))
    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: StasjonPostForm) {
        every { StasjonRepository.exists("notUnique") } returns true

        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}