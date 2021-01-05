//package no.oslokommune.ombruk.uttaksdata.form
//
//import io.swagger.v3.oas.annotations.media.Schema
//import kotlinx.serialization.Serializable
//import no.oslokommune.ombruk.shared.form.IForm
//import no.oslokommune.ombruk.shared.model.serializer.LocalDateTimeSerializer
//import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
//import no.oslokommune.ombruk.uttak.database.UttakRepository
//import org.valiktor.functions.isGreaterThan
//import org.valiktor.functions.isValid
//import org.valiktor.validate
//import java.time.LocalDateTime
//
//@Serializable
//data class UttaksdataPostForm(
//    @field:Schema(
//        description = "The ID of the UttaksData",
//        required = true
//    ) val uttakID: Int, //Not sure why this is exposed?
//    @field:Schema(description = "The weight of the UttaksData", required = true) val vekt: Int,
//    @field:Schema(
//        description = "The time at which the weight was reported",
//        required = true
//    ) @Serializable(with = LocalDateTimeSerializer::class) val rapportertTidspunkt: LocalDateTime
//) : IForm<UttaksdataPostForm> {
//    override fun validOrError() = runCatchingValidation {
//        validate(this) {
//            validate(UttaksdataPostForm::uttakID).isGreaterThan(0)
//            validate(UttaksdataPostForm::vekt).isGreaterThan(0)
//
//            validate(UttaksdataPostForm::uttakID).isGreaterThan(0).isValid {
//                UttakRepository.exists(uttakID)
//            }
//        }
//    }
//
//}