package pickup.form

import arrow.core.Either
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.partner.model.Partner
import ombruk.backend.pickup.database.PickupRepository
import ombruk.backend.pickup.form.pickup.PickupUpdateForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.shared.database.initDB
import org.junit.jupiter.api.AfterAll
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
class PickupUpdateFormTest {

    val existingPickup = Pickup(
            1,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "test",
            Station(1, "test"),
            Partner(1, "test")
        )

    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        mockkObject(PartnerRepository)
        mockkObject(PickupRepository)
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
        PickupUpdateForm(1),
        PickupUpdateForm(1, LocalDateTime.now()),
        PickupUpdateForm(1, endDateTime = LocalDateTime.now().plusHours(1)),
        PickupUpdateForm(1, LocalDateTime.now(), LocalDateTime.now().plusHours(1)),
        PickupUpdateForm(1, description = "test"),
        PickupUpdateForm(1, chosenPartnerId = 1),
        PickupUpdateForm(1, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "test", 1)
        )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: PickupUpdateForm) {
        every { PickupRepository.getPickupById(existingPickup.id) } returns existingPickup.right()
        every { PartnerRepository.exists(1) } returns true

        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        PickupUpdateForm(0),
        PickupUpdateForm(1, LocalDateTime.now().plusHours(2)),
        PickupUpdateForm(1, endDateTime = LocalDateTime.now().minusHours(2)),
        PickupUpdateForm(1, LocalDateTime.now(), LocalDateTime.now().minusHours(1)),
        PickupUpdateForm(1, description = ""),
        PickupUpdateForm(1, chosenPartnerId = 0),
        PickupUpdateForm(1, chosenPartnerId = 2)
    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: PickupUpdateForm) {

        every { PickupRepository.getPickupById(existingPickup.id) } returns existingPickup.right()
        every { PartnerRepository.exists(1) } returns true
        every { PartnerRepository.exists(2) } returns false

        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}