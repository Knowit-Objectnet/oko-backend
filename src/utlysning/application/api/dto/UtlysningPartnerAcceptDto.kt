package ombruk.backend.utlysning.application.api.dto

import arrow.core.Either
import io.ktor.locations.*
import kotlinx.serialization.Serializable
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.utils.validation.runCatchingValidation
import ombruk.backend.utlysning.domain.params.UtlysningPartnerAcceptParams
import ombruk.backend.utlysning.domain.port.IUtlysningRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.valiktor.functions.isValid
import org.valiktor.validate
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
@KtorExperimentalLocationsAPI
@Location("/partner-aksepter")
data class UtlysningPartnerAcceptDto(
    @Serializable(with = UUIDSerializer::class) override val id: UUID,
    override val toAccept: Boolean
) : IForm<UtlysningPartnerAcceptDto>, UtlysningPartnerAcceptParams(), KoinComponent {
    override fun validOrError() = runCatchingValidation {
        //TODO: This validation is based on a first-come-first-serve, if we want manual processing,
        // we need a different validation
        validate(this) {

            if (toAccept) {
                val utlysningRepository: IUtlysningRepository by inject()

                val myUtlysning = transaction { utlysningRepository.findOne(this@UtlysningPartnerAcceptDto.id) }
                require(myUtlysning is Either.Right)

                val allAccepted = transaction {
                    utlysningRepository.find(
                        UtlysningFindDto(partnerPameldt = true, hentingId = myUtlysning.b.hentingId)
                    )
                }
                require(allAccepted is Either.Right)

                validate(UtlysningPartnerAcceptDto::toAccept).isValid { allAccepted.b.isEmpty() }
            }
        }
    }
}