package no.oslokommune.ombruk.partner.service

import arrow.core.Either
import arrow.core.extensions.either.monad.flatMap
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.partner.form.PartnerGetForm
import no.oslokommune.ombruk.partner.form.PartnerPostForm
import no.oslokommune.ombruk.partner.form.PartnerUpdateForm
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.shared.api.KeycloakGroupIntegration
import no.oslokommune.ombruk.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction

object PartnerService : IPartnerService {

    @KtorExperimentalAPI
    override fun savePartner(partnerForm: PartnerPostForm): Either<ServiceError, Partner> = transaction {
        PartnerRepository.insertPartner(partnerForm).flatMap { partner ->
            KeycloakGroupIntegration.createGroup(partner.name, partner.id)
                .bimap({ rollback(); it }, { partner })
        }
    }

    override fun getPartnerById(id: Int): Either<ServiceError, Partner> = PartnerRepository.getPartnerByID(id)

    @KtorExperimentalLocationsAPI
    override fun getPartners(partnerGetForm: PartnerGetForm): Either<ServiceError, List<Partner>> =
        PartnerRepository.getPartners(partnerGetForm)

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