package no.oslokommune.ombruk.station.form

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.station.database.StationRepository
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.model.serializer.LocalTimeSerializer
import no.oslokommune.ombruk.shared.utils.validation.isUniqueInRepository
import no.oslokommune.ombruk.shared.utils.validation.isValid
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
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
            validate(StationPostForm::name).isUniqueInRepository(StationRepository)
        }
    }
}