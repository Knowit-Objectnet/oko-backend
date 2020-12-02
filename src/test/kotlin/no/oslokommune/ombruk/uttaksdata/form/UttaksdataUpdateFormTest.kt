package no.oslokommune.ombruk.uttaksdata.form

import arrow.core.Either
import no.oslokommune.ombruk.uttak.database.UttakRepository
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import no.oslokommune.ombruk.shared.database.initDB
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/*
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UttaksdataUpdateFormTest {

    @BeforeEach
    fun setup() {
        mockkObject(UttakRepository)
    }

    @AfterAll
    fun finish() {
        unmockkAll()
    }

    @Suppress("unused")
    fun generateValidForms() = listOf(
        UttaksdataUpdateForm(1, 123)
    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: UttaksdataUpdateForm) {
        every { UttakRepository.exists(123) } returns true

        val result = form.validOrError()

        println(result)
        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        UttaksdataUpdateForm(1, 0),
        UttaksdataUpdateForm(0, 1)

    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: UttaksdataUpdateForm) {

        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }
}
 */