package pickup.form

import arrow.core.Either
import ombruk.backend.pickup.form.pickup.PickupGetForm
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PickupGetFormTest {

    @Suppress("unused")
    fun generateValidForms() = listOf(
        PickupGetForm(),
        PickupGetForm(stationId = 1),
        PickupGetForm(partnerId = 1),
        PickupGetForm(LocalDateTime.now()),
        PickupGetForm(endDateTime = LocalDateTime.now()),
        PickupGetForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1)),
        PickupGetForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1), 1, 1)
        )

    @ParameterizedTest
    @MethodSource("generateValidForms")
    fun `validate valid form`(form: PickupGetForm) {
        val result = form.validOrError()

        require(result is Either.Right)
        assertEquals(form, result.b)
    }

    @Suppress("unused")
    fun generateInvalidForms() = listOf(
        PickupGetForm(stationId = 0),
        PickupGetForm(partnerId = 0),
        PickupGetForm(LocalDateTime.now(), LocalDateTime.now().minusHours(1))
    )

    @ParameterizedTest
    @MethodSource("generateInvalidForms")
    fun `validate invalid form`(form: PickupGetForm) {
        val result = form.validOrError()

        assertTrue(result is Either.Left)
    }

}