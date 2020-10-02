package no.oslokommune.ombruk.uttak.form

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@KtorExperimentalLocationsAPI
@Location("/{id}")
data class UttakGetByIdForm(val id: Int) : IForm<UttakGetByIdForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(UttakGetByIdForm::id).isGreaterThan(0)
        }
    }
}