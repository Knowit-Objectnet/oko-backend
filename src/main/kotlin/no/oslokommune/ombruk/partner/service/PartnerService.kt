package no.oslokommune.ombruk.partner.service

import arrow.core.Either
import arrow.core.extensions.either.monad.flatMap
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import no.oslokommune.ombruk.partner.database.SamPartnerRepository
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
        SamPartnerRepository.insertPartner(partnerForm).flatMap { partner ->
            KeycloakGroupIntegration.createGroup(partner.navn, partner.id)
                .bimap({ rollback(); it }, { partner })
        }
    }

    override fun getPartnerById(id: Int): Either<ServiceError, Partner> = SamPartnerRepository.getPartnerByID(id)

    @KtorExperimentalLocationsAPI
    override fun getPartnere(partnerGetForm: PartnerGetForm): Either<ServiceError, List<Partner>> =
        SamPartnerRepository.getPartnere(partnerGetForm)

    @KtorExperimentalAPI
    override fun deletePartnerById(id: Int): Either<ServiceError, Partner> = transaction {
        getPartnerById(id).flatMap { partner ->
            SamPartnerRepository.deletePartner(id)
                .flatMap { KeycloakGroupIntegration.deleteGroup(partner.navn) }
                .bimap({ rollback(); it }, { partner })
        }
    }


    @KtorExperimentalAPI
    override fun updatePartner(partnerForm: PartnerUpdateForm): Either<ServiceError, Partner> = transaction {
        getPartnerById(partnerForm.id).flatMap { partner ->
            SamPartnerRepository.updatePartner(partnerForm).flatMap { newPartner ->
                KeycloakGroupIntegration.updateGroup(partner.navn, newPartner.navn)
                    .bimap({ rollback(); it }, { newPartner })
            }
        }
    }
}