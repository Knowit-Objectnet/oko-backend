package no.oslokommune.ombruk.uttaksdata.form

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.utils.validation.isInRepository
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import no.oslokommune.ombruk.uttak.database.UttakRepository
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isValid
import org.valiktor.validate

@Serializable
data class UttaksDataUpdateForm(
    @field:Schema(description = "The ID of the Uttaksdata to update", required = true) val uttakId: Int,
//    @field:Schema(
//        description = "The ID of the Uttak that corresponds to this Uttaksdata",
//        required = true
//    ) val uttakID: Int, // Required to verify ownership. Comment to comment: Not really. The Uttaksdata ID should correspond to an Uttak ID.
    @field:Schema(description = "The new weight to be associated with the Uttaksdata") val vekt: Int? = null
//    @field:Schema(
//        description = "The time of which the Uttaksdata was updated"
//    ) @Serializable(with = LocalDateTimeSerializer::class) val rapportertTidspukt: LocalDateTime? = null // This should be handled by the server, not the client.
) : IForm<UttaksDataUpdateForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(UttaksDataUpdateForm::uttakId).isGreaterThan(0)
//            validate(UttaksdataUpdateForm::uttakId).isGreaterThan(0).isValid {
//                UttakRepository.exists(it)
//            }
//            validate(UttaksDataUpdateForm::uttakId).isInRepository(UttakRepository)
            validate(UttaksDataUpdateForm::vekt).isGreaterThan(0)
//            validate(UttaksdataUpdateForm::rapportertTidspukt).isLessThan(LocalDateTime.now())
        }
    }

}