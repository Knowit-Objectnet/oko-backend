package ombruk.backend.calendar.form.station

import kotlinx.serialization.Serializable
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.serializer.LocalTimeSerializer
import ombruk.backend.shared.utils.validation.isGreaterThanOpeningTime
import ombruk.backend.shared.utils.validation.isLessThanClosingTime
import ombruk.backend.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.time.LocalTime

@Serializable
data class StationUpdateForm(
    val id: Int,
    val name: String? = null,
    @Serializable(with = LocalTimeSerializer::class) val openingTime: LocalTime? = null,
    @Serializable(with = LocalTimeSerializer::class) val closingTime: LocalTime? = null
) : IForm<StationUpdateForm> {
    override fun validOrError()= runCatchingValidation {
        validate(this) {
            validate(StationUpdateForm::id).isGreaterThan(0)
            validate(StationUpdateForm::name).isNotBlank()
            StationRepository.getStationById(id).map {
                val newOpeningTime = openingTime ?: it.openingTime
                val newClosingTime = closingTime ?: it.closingTime
                validate(StationUpdateForm::openingTime).isLessThanClosingTime(newClosingTime)
                validate(StationUpdateForm::closingTime).isGreaterThanOpeningTime(newOpeningTime)
            }

        }
    }
}