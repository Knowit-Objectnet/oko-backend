package no.oslokommune.ombruk.uttaksdata.form

import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@Location("/{id}")
data class UttaksdataGetByIdForm(
    var id: Int
) : IForm<UttaksdataGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(UttaksdataGetByIdForm::id).isGreaterThan(0)

        }
    }
}