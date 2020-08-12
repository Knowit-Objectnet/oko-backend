package ombruk.backend.calendar.form.station

import kotlinx.serialization.Serializable
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalTimeSerializer
import ombruk.backend.shared.utils.validation.isStationUnique
import ombruk.backend.shared.utils.validation.isValid
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.time.DayOfWeek
import java.time.LocalTime

@Serializable
data class StationPostForm(
    val name: String,
    val hours: Map<DayOfWeek, List<@Serializable(with = LocalTimeSerializer::class) LocalTime>>? = null
) : IForm<StationPostForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StationPostForm::name).isNotBlank()
            validate(StationPostForm::hours).isValid()
            validate(StationPostForm::name).isStationUnique(StationRepository)
        }
    }
}