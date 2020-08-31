package calendar.form.event

import arrow.core.Either
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.calendar.form.event.EventUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.database.initDB
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

    private val existingStation = Station(id = 1, name = "some station", hours = openHours())
    private val existingEvent =
        Event(1, LocalDateTime.now(), LocalDateTime.now().plusHours(1), existingStation, Partner(1, "name"))


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
    fun generateValidForms() = listOf(
        EventUpdateForm(1, LocalDateTime.now()),
        EventUpdateForm(1, endDateTime = LocalDateTime.now()),
        EventUpdateForm(1, LocalDateTime.now(), LocalDateTime.now().plusHours(1))

    )

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
        EventUpdateForm(1, LocalDateTime.now().plusHours(2)),
        EventUpdateForm(1, endDateTime = LocalDateTime.now().minusHours(2)),
        EventUpdateForm(1, LocalDateTime.now(), LocalDateTime.now().minusHours(1))
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