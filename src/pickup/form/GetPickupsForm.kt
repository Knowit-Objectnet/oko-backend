package ombruk.backend.pickup.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import java.time.LocalDateTime

@KtorExperimentalLocationsAPI
@Location("/")
data class GetPickupsForm (
    var startTime: LocalDateTime? = null,
    var endTime: LocalDateTime? = null,
    val stationId: Int? = null
)