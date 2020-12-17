package no.oslokommune.ombruk.uttaksdata.form

import io.swagger.v3.oas.annotations.media.Schema
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
    @field:Schema(description = "The ID of the Uttaksdata to update", required = true) val id: Int,
//    @field:Schema(
//        description = "The ID of the Uttak that corresponds to this Uttaksdata",
//        required = true
//    ) val uttakID: Int, // Required to verify ownership. Comment to comment: Not really. The Uttaksdata ID should correspond to an Uttak ID.
    @field:Schema(description = "The new weight to be associated with the Uttaksdata") val vekt: Int? = null
//    @field:Schema(
//        description = "The time of which the Uttaksdata was updated"
//    ) @Serializable(with = LocalDateTimeSerializer::class) val rapportertTidspukt: LocalDateTime? = null // This should be handled by the server, not the client.
) : IForm<UttaksdataUpdateForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(UttaksdataUpdateForm::id).isGreaterThan(0).isValid { UttakRepository.exists(it) }
//            validate(UttaksdataUpdateForm::uttakId).isGreaterThan(0).isValid {
//                UttakRepository.exists(it)
//            }
            validate(UttaksdataUpdateForm::vekt).isGreaterThan(0)
//            validate(UttaksdataUpdateForm::rapportertTidspukt).isLessThan(LocalDateTime.now())
        }
    }

}