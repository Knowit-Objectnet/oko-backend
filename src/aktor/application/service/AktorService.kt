package ombruk.backend.aktor.application.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.aktor.domain.entity.Aktor
import ombruk.backend.aktor.domain.port.IPartnerRepository
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.aktor.domain.enum.AktorType
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class AktorService(
    private val stasjonRepository: IStasjonRepository,
    private val partnerRepository: IPartnerRepository,
    //val keycloakGroupIntegration: KeycloakGroupIntegration
) : IAktorService {

    override fun findOne(id: UUID): Either<ServiceError, Aktor> {
        // Check if ID exists in stasjon
        return transaction {
             stasjonRepository.findOne(id).fold(
                {
                    // Check if ID exists in partner
                    partnerRepository.findOne(id).fold(
                        { ServiceError("Not found").left() },
                        { Aktor(it.id, it.navn, listOf(), AktorType.PARTNER).right() }
                    )
                },
                { Aktor(it.id, it.navn, listOf(), AktorType.STASJON).right() }
            )
        }
    }
}