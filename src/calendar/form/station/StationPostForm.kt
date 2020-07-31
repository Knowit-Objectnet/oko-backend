package ombruk.backend.calendar.form.station

import kotlinx.serialization.Serializable
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalTimeSerializer
import ombruk.backend.shared.utils.validation.isLessThanClosingTime
import ombruk.backend.shared.utils.validation.isStationUnique
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.time.LocalTime

@Serializable
data class StationPostForm(
    val name: String,
    @Serializable(with = LocalTimeSerializer::class) val openingTime: LocalTime,
    @Serializable(with = LocalTimeSerializer::class) val closingTime: LocalTime
) : IForm<StationPostForm> {
    override fun validOrError()= runCatchingValidation {
        validate(this) {
            validate(StationPostForm::name).isNotBlank()
            validate(StationPostForm::openingTime).isLessThanClosingTime(closingTime)
            validate(StationPostForm::name).isStationUnique(StationRepository)
        }
    }
}