package calendar.form.event

import arrow.core.Either
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.calendar.form.event.EventUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.model.Partner
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventUpdateFormTest {

    private val existingEvent =
        Event(1, LocalDateTime.now(), LocalDateTime.now().plusHours(1), Station(1, "test"), Partner(1, "name"))

    @BeforeEach
    fun setup() {
        mockkObject(EventRepository)
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
        EventUpdateForm(1, LocalDateTime.now()),
        EventUpdateForm(1, endDateTime = LocalDateTime.now()),
        EventUpdateForm(1, LocalDateTime.now(), LocalDateTime.now().plusHours(1))

    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: EventUpdateForm) {
        every { EventRepository.getEventByID(1) } returns existingEvent.right()

        val result = form.validOrError()
        println(result)
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

}