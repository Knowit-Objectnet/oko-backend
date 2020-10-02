package no.oslokommune.ombruk.uttaksdata.form

import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isLessThanEndDateTime
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNull
import org.valiktor.validate
import java.time.LocalDateTime

@Location("/")
data class UttaksdataGetForm(
    var uttakId: Int? = null,
    var stasjonId: Int? = null,
    var partnerId: Int? = null,
    var fromDate: LocalDateTime? = null,
    var toDate: LocalDateTime? = null
) : IForm<UttaksdataGetForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(UttaksdataGetForm::uttakId).isGreaterThan(0)
            validate(UttaksdataGetForm::stasjonId).isGreaterThan(0)
            validate(UttaksdataGetForm::partnerId).isGreaterThan(0)

            validate(UttaksdataGetForm::fromDate).isLessThanEndDateTime(toDate)

            if (uttakId != null) {
                validate(UttaksdataGetForm::stasjonId).isNull()
                validate(UttaksdataGetForm::partnerId).isNull()
                validate(UttaksdataGetForm::fromDate).isNull()
                validate(UttaksdataGetForm::toDate).isNull()
            }
        }
    }
}