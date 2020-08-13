package calendar.form.event

import arrow.core.Either
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.model.RecurrenceRule
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.shared.database.initDB
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class EventPostFormTest {

    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        mockkObject(StationRepository)
        mockkObject(PartnerRepository)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Suppress("unused")
    fun generateValidForms() = listOf(
        EventPostForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1), 1, 1),
        EventPostForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1), 1, 1, RecurrenceRule(1, count = 1)),
        EventPostForm(
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            1,
            1,
            RecurrenceRule(1, count = 1, interval = 1)
        ),
        EventPostForm(
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            1,
            1,
            RecurrenceRule(1, until = LocalDateTime.now().plusDays(1))
        )
    )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: EventPostForm) {
        every { StationRepository.exists(1) } returns true
        every { PartnerRepository.exists(1) } returns true

        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        EventPostForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1), 1, 2),
        EventPostForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1), 2, 1),
        EventPostForm(LocalDateTime.now(), LocalDateTime.now().minusHours(1), 1, 1),
        EventPostForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1), 1, 1, RecurrenceRule(1)),
        EventPostForm(
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            1,
            1,
            RecurrenceRule(1, count = 0)
        ),
        EventPostForm(
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            1,
            1,
            RecurrenceRule(1, count = 1, interval = 0)
        ),
        EventPostForm(
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            1,
            1,
            RecurrenceRule(1, until = LocalDateTime.now().minusDays(1))
        )
    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: EventPostForm) {
        every { StationRepository.exists(1) } returns true
        every { PartnerRepository.exists(1) } returns true

        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}