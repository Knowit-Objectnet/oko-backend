package no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel

import arrow.core.Either
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.shared.error.ValidationError
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isInRepository
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import no.oslokommune.ombruk.uttak.database.UttakRepository
import org.valiktor.functions.isGreaterThan
import org.valiktor.validate

@Serializable
data class UttaksforesporselPostForm(
    val uttaksId: Int,
    val partnerId: Int
) : IForm<UttaksforesporselPostForm> {
    override fun validOrError(): Either<ValidationError, UttaksforesporselPostForm> = runCatchingValidation {
        validate(this) {
            validate(UttaksforesporselPostForm::uttaksId).isGreaterThan(0)
            validate(UttaksforesporselPostForm::partnerId).isGreaterThan(0)
            validate(UttaksforesporselPostForm::uttaksId).isInRepository(UttakRepository)
            validate(UttaksforesporselPostForm::partnerId).isInRepository(PartnerRepository)
        }
    }
}