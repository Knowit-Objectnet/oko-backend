package no.oslokommune.ombruk.uttaksforesporsel.form

import arrow.core.Either
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.uttaksforesporsel.database.PickupRepository
import no.oslokommune.ombruk.uttaksforesporsel.form.pickup.PickupUpdateForm
import no.oslokommune.ombruk.uttaksforesporsel.model.Pickup
import no.oslokommune.ombruk.shared.database.initDB
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
        Stasjon(1, "test"),
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
    fun finish() {
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