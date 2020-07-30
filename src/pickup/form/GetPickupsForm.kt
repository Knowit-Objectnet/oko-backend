package ombruk.backend.pickup.form

import arrow.core.Either
import calendar.form.EventGetForm
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import kotlinx.serialization.Serializable
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalDateTimeSerializer
import ombruk.backend.shared.utils.validation.runCatchingValidation
import java.time.LocalDateTime

@KtorExperimentalLocationsAPI
@Location("/")
data class GetPickupsForm (
    var startTime: LocalDateTime? = null,
    var endTime: LocalDateTime? = null,
    val stationId: Int? = null
)