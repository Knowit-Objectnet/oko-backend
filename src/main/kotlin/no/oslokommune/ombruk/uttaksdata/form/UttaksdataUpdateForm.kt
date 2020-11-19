package no.oslokommune.ombruk.uttaksdata.form

import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import no.oslokommune.ombruk.uttak.database.UttakRepository
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isLessThan
import org.valiktor.functions.isValid
import org.valiktor.validate
import java.time.LocalDateTime

@Serializable
data class UttaksdataUpdateForm(
    val id: Int,
    val uttakID: Int, // Required to verify ownership
    val vekt: Int? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val rapportertTidspukt: LocalDateTime? = null
) : IForm<UttaksdataUpdateForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(UttaksdataUpdateForm::id).isGreaterThan(0)
            validate(UttaksdataUpdateForm::uttakID).isGreaterThan(0).isValid {
                UttakRepository.exists(it)
            }
            validate(UttaksdataUpdateForm::vekt).isGreaterThan(0)
            validate(UttaksdataUpdateForm::rapportertTidspukt).isLessThan(LocalDateTime.now())
        }
    }

}