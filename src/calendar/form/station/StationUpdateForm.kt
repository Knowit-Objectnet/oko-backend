package ombruk.backend.calendar.form.station

import kotlinx.serialization.Serializable
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalTimeSerializer
import ombruk.backend.shared.utils.validation.*
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.time.DayOfWeek
import java.time.LocalTime

@Serializable
data class StationUpdateForm(
    val id: Int,
    val name: String?,
    val hours: Map<DayOfWeek, List<@Serializable(with = LocalTimeSerializer::class) LocalTime>>?
) : IForm<StationUpdateForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StationUpdateForm::id).isGreaterThan(0)
            validate(StationUpdateForm::name).isNotBlank()
            validate(StationUpdateForm::hours).isValid()

        }
    }
}