package no.oslokommune.ombruk.stasjon.form

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
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
data class StasjonUpdateForm(
    val id: Int,
    val name: String? = null,
    val hours: Map<DayOfWeek, List<@Serializable(with = LocalTimeSerializer::class) LocalTime>>? = null
) : IForm<StasjonUpdateForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StasjonUpdateForm::id).isGreaterThan(0)
            validate(StasjonUpdateForm::name).isNotBlank()
            validate(StasjonUpdateForm::name).isUniqueInRepository(StasjonRepository)
            validate(StasjonUpdateForm::hours).isValid()

        }
    }
}