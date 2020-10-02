package no.oslokommune.ombruk.uttak.form

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import no.oslokommune.ombruk.shared.utils.validation.isLessThanEndDateTime
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNull
import org.valiktor.validate
import java.time.LocalDateTime


@KtorExperimentalLocationsAPI
@Location("/")
data class UttakGetForm(
    val uttakId: Int? = null,
    val stationId: Int? = null,
    val partnerId: Int? = null,
    val recurrenceRuleId: Int? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val fromDate: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val toDate: LocalDateTime? = null
) : IForm<UttakGetForm> {

    override fun validOrError(): Either<ValidationError, UttakGetForm> = runCatchingValidation {
        validate(this) {
            validate(UttakGetForm::uttakId).isGreaterThan(0)
            validate(UttakGetForm::stationId).isGreaterThan(0)
            validate(UttakGetForm::partnerId).isGreaterThan(0)
            validate(UttakGetForm::recurrenceRuleId).isGreaterThan(0)

            if (fromDate != null && toDate != null) validate(UttakGetForm::fromDate).isLessThanEndDateTime(toDate)

            if (uttakId != null) {
                validate(UttakGetForm::stationId).isNull()
                validate(UttakGetForm::partnerId).isNull()
                validate(UttakGetForm::recurrenceRuleId).isNull()
                validate(UttakGetForm::fromDate).isNull()
                validate(UttakGetForm::toDate).isNull()
            }

        }
    }
}

