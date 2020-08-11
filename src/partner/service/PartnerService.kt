package ombruk.backend.partner.service

import arrow.core.Either
import arrow.core.extensions.either.monad.flatMap
import arrow.core.left
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.partner.form.PartnerGetForm
import ombruk.backend.partner.form.PartnerPostForm
import ombruk.backend.partner.form.PartnerUpdateForm
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.api.KeycloakGroupIntegration
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction

object PartnerService : IPartnerService {

    @KtorExperimentalAPI
    override fun savePartner(partnerForm: PartnerPostForm) = transaction {
        PartnerRepository.insertPartner(partnerForm).flatMap { partner ->
            KeycloakGroupIntegration.createGroup(partner.name, partner.id)
                .bimap({ rollback(); it }, { partner })
        }
    }

    override fun getPartnerById(id: Int) = PartnerRepository.getPartnerByID(id)

    @KtorExperimentalLocationsAPI
    override fun getPartners(partnerGetForm: PartnerGetForm) = PartnerRepository.getPartners(partnerGetForm)

    @KtorExperimentalAPI
    override fun deletePartnerById(id: Int): Either<ServiceError, Partner> = transaction {
        getPartnerById(id).flatMap { partner ->
            PartnerRepository.deletePartner(id)
                .flatMap { KeycloakGroupIntegration.deleteGroup(partner.name) }
                .bimap({ rollback(); it }, { partner })
        }
    }


    @KtorExperimentalAPI
    override fun updatePartner(partnerForm: PartnerUpdateForm): Either<ServiceError, Partner> = transaction {
        getPartnerById(partnerForm.id).flatMap { partner ->
            PartnerRepository.updatePartner(partnerForm).flatMap { newPartner ->
                KeycloakGroupIntegration.updateGroup(partner.name, newPartner.name)
                    .bimap({ rollback(); it }, { newPartner })
            }
        }
    }
}