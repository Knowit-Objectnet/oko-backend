package no.oslokommune.ombruk.station.form

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.station.database.StationRepository
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.model.serializer.LocalTimeSerializer
import no.oslokommune.ombruk.shared.utils.validation.isUniqueInRepository
import no.oslokommune.ombruk.shared.utils.validation.isValid
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.time.DayOfWeek
import java.time.LocalTime

@Serializable
data class StationUpdateForm(
    val id: Int,
    val name: String? = null,
    val hours: Map<DayOfWeek, List<@Serializable(with = LocalTimeSerializer::class) LocalTime>>? = null
) : IForm<StationUpdateForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StationUpdateForm::id).isGreaterThan(0)
            validate(StationUpdateForm::name).isNotBlank()
            validate(StationUpdateForm::name).isUniqueInRepository(StationRepository)
            validate(StationUpdateForm::hours).isValid()

        }
    }
}