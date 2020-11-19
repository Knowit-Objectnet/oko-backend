package no.oslokommune.ombruk.uttak.form

import arrow.core.Either
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.shared.database.initDB
import no.oslokommune.ombruk.uttak.model.UttaksType
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
class UttakPostFormTest {

    private val existingStasjon = Stasjon(id = 1, navn = "some stasjon", aapningstider = openHours())

    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        mockkObject(StasjonRepository)
        mockkObject(PartnerRepository)
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
    fun generateValidForms(): List<UttakPostForm> {
        val from = LocalDateTime.parse("2020-09-02T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
        return listOf(
            UttakPostForm(
                stasjonId = 1,
                partnerId = 1,
                gjentakelsesRegel = GjentakelsesRegel(
                    sluttTidspunkt = LocalDateTime.now().plusDays(1),
                    dager = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
                ),
                type = UttaksType.GJENTAKENDE,
                startTidspunkt = from,
                sluttTidspunkt = from.plusHours(5))
        )
    }

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: UttakPostForm) {
        every { PartnerRepository.exists(1) } returns true
        every { StasjonRepository.exists(existingStasjon.id) } returns true
        every { StasjonRepository.getStasjonById(1) } returns existingStasjon.right()

        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms(): List<UttakPostForm> {
        val from = LocalDateTime.parse("2020-09-02T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
        return listOf(
            UttakPostForm(
                -2,
                1,
                GjentakelsesRegel(
                    1,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusHours(1),
                    null,
                    listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
                ),
                UttaksType.GJENTAKENDE,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1))
        )
    }

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: UttakPostForm) {
        every { StasjonRepository.exists(1) } returns true
        every { PartnerRepository.exists(1) } returns true
        every { StasjonRepository.getStasjonById(1) } returns Either.right(Stasjon(
            id = 1, navn = "some stasjon", aapningstider = openHours()
        ))

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