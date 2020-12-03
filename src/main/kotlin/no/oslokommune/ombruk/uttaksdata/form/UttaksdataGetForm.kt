package no.oslokommune.ombruk.uttaksdata.form

import io.ktor.locations.Location
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isLessThanEndDateTime
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate
import java.time.LocalDateTime

@Location("/")
data class UttaksdataGetForm(
    val uttakId: Int? = null,
    val minVekt: Int? = null,
    val maxVekt: Int? = Integer.MAX_VALUE,
    val fraRapportertTidspunkt: LocalDateTime? = LocalDateTime.MIN,
    val tilRapportertTidspunkt: LocalDateTime? = LocalDateTime.MAX
) : IForm<UttaksdataGetForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(UttaksdataGetForm::uttakId).isGreaterThan(0)

            validate(UttaksdataGetForm::minVekt).isGreaterThan(0)
            validate(UttaksdataGetForm::maxVekt).isGreaterThan(0)
            if (minVekt != null && maxVekt !== null) validate(UttaksdataGetForm::maxVekt).isGreaterThan(minVekt)

            if (fraRapportertTidspunkt != null && tilRapportertTidspunkt != null)
                validate(UttaksdataGetForm::fraRapportertTidspunkt).isLessThanEndDateTime(tilRapportertTidspunkt)
        }
    }
}