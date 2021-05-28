package ombruk.backend.aktor.application.service

import arrow.core.Either
import arrow.core.extensions.either.monad.flatMap
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import ombruk.backend.aktor.application.api.dto.PartnerGetDto
import ombruk.backend.aktor.application.api.dto.PartnerSaveDto
import ombruk.backend.aktor.application.api.dto.PartnerUpdateDto
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.port.IPartnerRepository
import ombruk.backend.shared.api.KeycloakGroupIntegration
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class PartnerService constructor(
    private val keycloakGroupIntegration: KeycloakGroupIntegration,
    private val partnerRepository: IPartnerRepository
) : IPartnerService {

    @KtorExperimentalAPI
    override fun savePartner(dto: PartnerSaveDto): Either<ServiceError, Partner> = transaction {
        partnerRepository.insert(dto).flatMap { partner ->
            keycloakGroupIntegration.createGroup(partner.navn, partner.id)
                .bimap({ rollback(); it }, { partner })
        }
    }

    override fun getPartnerById(id: UUID): Either<ServiceError, Partner> = transaction{ partnerRepository.findOne(id) }

    @KtorExperimentalLocationsAPI
    override fun getPartnere(dto: PartnerGetDto): Either<ServiceError, List<Partner>> =
        transaction { partnerRepository.find(dto) }

    @KtorExperimentalAPI
    override fun deletePartnerById(id: UUID): Either<ServiceError, Partner> = transaction {
        getPartnerById(id).flatMap { partner ->
            partnerRepository.delete(id)
                .flatMap { keycloakGroupIntegration.deleteGroup(partner.navn) }
                .bimap({ rollback(); it }, { partner })
        }
    }


    @KtorExperimentalAPI
    override fun updatePartner(dto: PartnerUpdateDto): Either<ServiceError, Partner> = transaction {
        getPartnerById(dto.id).flatMap { partner ->
            partnerRepository.update(dto).flatMap { newPartner ->
                keycloakGroupIntegration.updateGroup(partner.navn, newPartner.navn)
                    .bimap({ rollback(); it }, { newPartner })
            }
        }
    }
}