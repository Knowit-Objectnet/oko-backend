package no.oslokommune.ombruk.event.form

import arrow.core.Either
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import no.oslokommune.ombruk.event.database.EventRepository
import no.oslokommune.ombruk.station.database.StationRepository
import no.oslokommune.ombruk.event.model.Event
import no.oslokommune.ombruk.station.model.Station
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
class EventUpdateFormTest {

    private val from = LocalDateTime.parse("2020-09-02T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
    private val existingStation = Station(id = 1, name = "some station", hours = openHours())
    private val existingEvent =
        Event(1, from, from.plusHours(1), existingStation, Partner(1, "name"))


    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        mockkObject(StationRepository)
        mockkObject(EventRepository)
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
    fun generateValidForms(): List<EventUpdateForm> {
        return listOf(
            EventUpdateForm(1, from),
            EventUpdateForm(1, endDateTime = from.plusHours(1)),
            EventUpdateForm(1, from, from.plusHours(1))

        )
    }

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: EventUpdateForm) {
        every { EventRepository.getEventByID(existingEvent.id) } returns existingEvent.right()
        every { StationRepository.exists(existingStation.id) } returns true
        every { StationRepository.getStationById(existingStation.id) } returns existingStation.right()

        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        EventUpdateForm(1),
        EventUpdateForm(1, from.plusHours(2)),
        EventUpdateForm(1, endDateTime = from.minusHours(2)),
        EventUpdateForm(1, from, from.minusHours(1))
    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: EventUpdateForm) {
        every { EventRepository.getEventByID(1) } returns existingEvent.right()

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