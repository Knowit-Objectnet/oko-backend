package no.oslokommune.ombruk.uttaksdata.form

import arrow.core.right
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.uttak.model.Uttak
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isValid
import org.valiktor.validate
import java.time.LocalDateTime

@Serializable
data class UttaksdataPostForm(
    val uttakID: Int,
    val vekt: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val rapportertTidspunkt: LocalDateTime
) : IForm<UttaksdataPostForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(UttaksdataPostForm::uttakID).isGreaterThan(0)
            validate(UttaksdataPostForm::vekt).isGreaterThan(0)

            validate(UttaksdataPostForm::uttakID).isGreaterThan(0).isValid {
                UttakRepository.getUttakByID(uttakID).isRight()
            }
        }
    }

}