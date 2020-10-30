package no.oslokommune.ombruk.uttak.form

import arrow.core.Either
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.shared.database.initDB
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class UttakUpdateFormTest {

    private val from = LocalDateTime.parse("2020-09-02T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
    private val existingStasjon = Stasjon(id = 1, name = "some stasjon", hours = openHours())
    private val existingUttak =
        Uttak(1, from, from.plusHours(1), existingStasjon, Partner(1, "test", "beskrivelse", "81549300", "test@test.com"))


    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        mockkObject(StasjonRepository)
        mockkObject(UttakRepository)
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
    fun generateValidForms(): List<UttakUpdateForm> {
        return listOf(
            UttakUpdateForm(1, from),
            UttakUpdateForm(1, sluttTidspunkt = from.plusHours(1)),
            UttakUpdateForm(1, from, from.plusHours(1))

        )
    }

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: UttakUpdateForm) {
        every { UttakRepository.getUttakByID(existingUttak.id) } returns existingUttak.right()
        every { StasjonRepository.exists(existingStasjon.id) } returns true
        every { StasjonRepository.getStasjonById(existingStasjon.id) } returns existingStasjon.right()

        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        UttakUpdateForm(1),
        UttakUpdateForm(1, from.plusHours(2)),
        UttakUpdateForm(1, sluttTidspunkt = from.minusHours(2)),
        UttakUpdateForm(1, from, from.minusHours(1))
    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: UttakUpdateForm) {
        every { UttakRepository.getUttakByID(1) } returns existingUttak.right()

        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

    private fun openHours() = mapOf<DayOfWeek, List<LocalTime>>(
        Pair(
            DayOfWeek.MONDAY,
            listOf(
                LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
            )
        ),
        Pair(
            DayOfWeek.TUESDAY,
            listOf(
                LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
            )
        ),
        Pair(
            DayOfWeek.WEDNESDAY,
            listOf(
                LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
            )
        ),
        Pair(
            DayOfWeek.THURSDAY,
            listOf(
                LocalTime.parse("10:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("21:00:00Z", DateTimeFormatter.ISO_TIME)
            )
        ),
        Pair(
            DayOfWeek.FRIDAY,
            listOf(
                LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("20:00:00Z", DateTimeFormatter.ISO_TIME)
            )
        )
    )
}