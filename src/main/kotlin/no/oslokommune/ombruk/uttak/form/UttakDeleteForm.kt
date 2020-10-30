package no.oslokommune.ombruk.uttak.form

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isGreaterThanStartDateTime
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
import java.time.LocalDateTime

@KtorExperimentalLocationsAPI
@Location("/")
data class UttakDeleteForm(
        var id: Int,
        var gjentakelsesRegelId: Int? = null,
        var fromDate: LocalDateTime? = null,
        var toDate: LocalDateTime? = null,
        var stasjonId: Int? = null,
        var partnerId: Int? = null
) : IForm<UttakDeleteForm> {
    override fun validOrError(): Either<ValidationError, UttakDeleteForm> = runCatchingValidation {
        validate(this) {

            validate(UttakDeleteForm::id).isGreaterThan(0)
            validate(UttakDeleteForm::gjentakelsesRegelId).isGreaterThan(0)
            validate(UttakDeleteForm::stasjonId).isGreaterThan(0)
            validate(UttakDeleteForm::partnerId).isGreaterThan(0)

            validate(UttakDeleteForm::toDate).isGreaterThanStartDateTime(fromDate)
        }
    }
}