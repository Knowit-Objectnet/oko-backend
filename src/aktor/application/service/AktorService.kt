package ombruk.backend.aktor.application.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.aktor.domain.port.IPartnerRepository
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.shared.api.KeycloakGroupIntegration
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.aktor.domain.enum.AktorType

class AktorService(
    private val stasjonRepository: IStasjonRepository,
    private val partnerRepository: IPartnerRepository,
    //val keycloakGroupIntegration: KeycloakGroupIntegration
) : IAktorService {

    override fun findOne(id: Int): Either<ServiceError, AktorType> {
        // Check if ID exists in stasjon
        return stasjonRepository.findOne(id).fold(
            {
                // Check if ID exists in partner
                partnerRepository.findOne(id).fold(
                    { ServiceError("Not found").left() },
                    { AktorType.PARTNER.right() }
                )
            },
            { AktorType.STASJON.right() }
        )
    }
}