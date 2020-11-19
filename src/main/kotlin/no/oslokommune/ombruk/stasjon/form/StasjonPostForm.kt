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
    val navn: String,
    val aapningstider: Map<DayOfWeek, List<@Serializable(with = LocalTimeSerializer::class) LocalTime>>
) : IForm<StasjonPostForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StasjonPostForm::navn).isNotBlank()
            validate(StasjonPostForm::aapningstider).isValid()
            validate(StasjonPostForm::navn).isUniqueInRepository(StasjonRepository)
        }
    }
}