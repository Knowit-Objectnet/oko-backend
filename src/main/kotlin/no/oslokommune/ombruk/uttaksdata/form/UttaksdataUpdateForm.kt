package no.oslokommune.ombruk.uttaksdata.form

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@Serializable
data class UttaksdataUpdateForm(val id: Int, val weight: Int) : IForm<UttaksdataUpdateForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(UttaksdataUpdateForm::id).isGreaterThan(0)
            validate(UttaksdataUpdateForm::weight).isGreaterThan(0)
        }
    }

}