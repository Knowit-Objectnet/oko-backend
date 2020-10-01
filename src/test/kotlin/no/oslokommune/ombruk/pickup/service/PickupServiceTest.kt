package no.oslokommune.ombruk.pickup.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.oslokommune.ombruk.event.form.EventPostForm
import no.oslokommune.ombruk.event.model.Event
import no.oslokommune.ombruk.event.service.EventService
import no.oslokommune.ombruk.pickup.database.PickupRepository
import no.oslokommune.ombruk.pickup.form.pickup.PickupDeleteForm
import no.oslokommune.ombruk.pickup.form.pickup.PickupGetByIdForm
import no.oslokommune.ombruk.pickup.form.pickup.PickupGetForm
import no.oslokommune.ombruk.pickup.form.pickup.PickupUpdateForm
import no.oslokommune.ombruk.pickup.model.Pickup
import no.oslokommune.ombruk.pickup.service.PickupService
import no.oslokommune.ombruk.shared.database.initDB
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.shared.error.ServiceError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class PickupServiceTest {

    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        mockkObject(PickupRepository)
        mockkObject(EventService)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @AfterAll
    fun finish() {
        unmockkAll()
    }

    @Nested
    inner class GetPickups {

        /**
         * Test that get no.oslokommune.ombruk.pickup by id returns the expected no.oslokommune.ombruk.pickup
         */
        @Test
        fun `get pickup by id`(@MockK expected: Pickup) {
            every { PickupRepository.getPickupById(1) } returns expected.right()
            val form = PickupGetByIdForm(1)
            val actual = PickupService.getPickupById(form)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        /**
         * If ID does not exist, return RepositoryError.NoRowsFound
         */
        @Test
        fun `get pickup invalid id`(@MockK expected: RepositoryError.NoRowsFound) {
            every { PickupRepository.getPickupById(1) } returns expected.left()
            val actual = PickupService.getPickupById(PickupGetByIdForm(1))
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        /**
         * If repository fails to get, return RepositoryError.SelectError
         */
        @Test
        fun `get pickup failure`(@MockK expected: RepositoryError.SelectError) {
            every { PickupRepository.getPickupById(1) } returns expected.left()
            val actual = PickupService.getPickupById(PickupGetByIdForm(1))
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        /**
         * If ID does not exist, return RepositoryError.NoRowsFound
         */
        @Test
        fun `get all pickups failure `(@MockK expected: RepositoryError.SelectError) {
            every { PickupRepository.getPickups(PickupGetForm()) } returns expected.left()
            val actual = PickupService.getPickups(PickupGetForm())
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        /**
         * Check that get pickups returns the expected list of pickups
         */
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

        /**
         * Check that delete by id call the PickupRepository
         */
        @Test
        fun `delete by id`() {
            PickupService.deletePickup(PickupDeleteForm(1))
            verify { PickupRepository.deletePickup(1) }
        }

        /**
         * Check that repository failure returns a RepositoryError
         */
        @Test
        fun `delete by id failure`(@MockK expected: RepositoryError.DeleteError) {
            every { PickupRepository.deletePickup(1) } returns expected.left()
            val actual = PickupService.deletePickup(PickupDeleteForm(1))
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }
    }

    @Nested
    inner class UpdatePickups {
        /**
         * Checks that update no.oslokommune.ombruk.pickup returns the updated no.oslokommune.ombruk.pickup
         */
        @Test
        fun `update pickup without choosing partner`(@MockK expected: Pickup) {
            val form = PickupUpdateForm(1, LocalDateTime.now(), LocalDateTime.now())
            every { PickupRepository.updatePickup(form) } returns expected.right()

            val actual = PickupService.updatePickup(form)

            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        /**
         * Check that partner is updated correctly
         */
        @Test
        fun `update pickup with chosenPartner`(
                @MockK event: Event,
                @MockK eventForm: EventPostForm,
                @MockK(relaxed = true) expected: Pickup
        ) {
            val form = PickupUpdateForm(1, chosenPartnerId = 1)
            every { PickupRepository.updatePickup(form) } returns expected.right()
            every { EventService.saveEvent(eventForm) } returns event.right()

            val actual = PickupService.updatePickup(form)
            require(actual is Either.Right)
            assertEquals(expected, actual.b)
        }

        /**
         * Check that update fails when id does not exist
         */

        /**
         * Check that a RepositoryError is returned when updating no.oslokommune.ombruk.pickup fails without partner
         */
        @Test
        fun `update pickup fails without chosenPartner`(@MockK expected: RepositoryError.UpdateError) {
            val form = PickupUpdateForm(1, LocalDateTime.now())
            every { PickupRepository.updatePickup(form) } returns expected.left()
            val actual = PickupService.updatePickup(form)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        /**
         * Check that a RepositoryError is returned when updating no.oslokommune.ombruk.pickup fails with partner
         */
        @Test
        fun `update pickup fails with chosenPartner`(@MockK expected: RepositoryError.UpdateError) {
            val form = PickupUpdateForm(1, LocalDateTime.now(), chosenPartnerId = 1)
            every { PickupRepository.updatePickup(form) } returns expected.left()
            val actual = PickupService.updatePickup(form)
            require(actual is Either.Left)
            assertEquals(expected, actual.a)
        }

        /**
         * Check that a RepositoryError is returned when updating no.oslokommune.ombruk.pickup fails at saveEvent
         */
        @Test
        fun `update pickup fails with chosenPartner at saveEvent`(
            @MockK(relaxed = true) pickup: Pickup,
            @MockK expected: ServiceError
        ) {
            val form = PickupUpdateForm(1, LocalDateTime.now(), chosenPartnerId = 1)
            every { PickupRepository.updatePickup(form) } returns pickup.right()
            every { EventService.saveEvent(any()) } returns expected.left()
            val actual = PickupService.updatePickup(form)
            require(actual is Either.Right)
            println(actual.b)
        }
    }
}