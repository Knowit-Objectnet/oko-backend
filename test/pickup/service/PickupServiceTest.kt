package pickup.service

import arrow.core.Either
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.verify
import ombruk.backend.calendar.model.Station
import ombruk.backend.pickup.database.PickupRepository
import ombruk.backend.pickup.form.pickup.PickupDeleteForm
import ombruk.backend.pickup.form.pickup.PickupGetByIdForm
import ombruk.backend.pickup.form.pickup.PickupUpdateForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.pickup.service.PickupService
import ombruk.backend.shared.database.initDB
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class PickupServiceTest {

    lateinit var testStation: Station
    lateinit var testStation2: Station

    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        mockkObject(PickupRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Nested
    inner class GetPickups {

        @Test
        fun `get pickup by id`(@MockK expected: Pickup) {
            every { PickupRepository.getPickupById(1) } returns expected.right()
            val form = PickupGetByIdForm(1)
            val actual = PickupService.getPickupById(form)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        @Test
        fun `get all pickups`(@MockK expected: List<Pickup>) {
            every { PickupRepository.getPickups(null) } returns expected.right()
            val actual = PickupService.getPickups()

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }
    }

    @Nested
    inner class DeletePickups {

        @Test
        fun `delete by id`() {
            PickupService.deletePickup(PickupDeleteForm(1))
            verify { PickupRepository.deletePickup(1) }
        }
    }

    @Nested
    inner class UpdatePickups {
        @Test
        fun `update pickup`(@MockK expected: Pickup) {
            val form = PickupUpdateForm(1, LocalDateTime.now(), LocalDateTime.now())
            every { PickupRepository.updatePickup(form) } returns expected.right()

            val actual = PickupService.updatePickup(form)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }
    }
}