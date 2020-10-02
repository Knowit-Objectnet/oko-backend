package no.oslokommune.ombruk.stasjon.form

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
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
data class StasjonPostForm(
    val name: String,
    val hours: Map<DayOfWeek, List<@Serializable(with = LocalTimeSerializer::class) LocalTime>>? = null
) : IForm<StasjonPostForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StasjonPostForm::name).isNotBlank()
            validate(StasjonPostForm::hours).isValid()
            validate(StasjonPostForm::name).isUniqueInRepository(StasjonRepository)
        }
    }
}