package ombruk.backend.aktor.application.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.list.traverse.sequence
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import ombruk.backend.aktor.application.api.dto.KontaktGetDto
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
    private val partnerRepository: IPartnerRepository,
    private val kontaktService: IKontaktService
) : IPartnerService {

    @KtorExperimentalAPI
    override fun savePartner(dto: PartnerSaveDto): Either<ServiceError, Partner> = transaction {
        partnerRepository.insert(dto).flatMap { partner ->
            partner.right() //keycloakGroupIntegration.createGroup(partner.navn, partner.id)
                .bimap({ rollback(); it }, { partner })
        }
    }

    override fun getPartnerById(id: UUID): Either<ServiceError, Partner> {
        return transaction {
            partnerRepository.findOne(id)
                .flatMap { partner ->
                    kontaktService.getKontakter(KontaktGetDto(aktorId = partner.id))
                        .flatMap { kontakter -> partner.copy(kontaktPersoner = kontakter).right() }
                }
        }
    }

    @KtorExperimentalLocationsAPI
    override fun getPartnere(dto: PartnerGetDto): Either<ServiceError, List<Partner>> {
        return transaction {
            partnerRepository.find(dto)
                .flatMap {
                    it.map { partner ->
                        kontaktService.getKontakter(KontaktGetDto(aktorId = partner.id))
                            .flatMap { kontakter -> partner.copy(kontaktPersoner = kontakter).right() }
                    }.sequence(Either.applicative()).fix().map { it.fix() }
                }
        }
    }

    @KtorExperimentalAPI
    override fun deletePartnerById(id: UUID): Either<ServiceError, Partner> = transaction {
        getPartnerById(id).flatMap { partner ->
            partnerRepository.delete(id)
                //.flatMap { keycloakGroupIntegration.deleteGroup(partner.navn) }
                .bimap({ rollback(); it }, { partner })
        }
    }


    @KtorExperimentalAPI
    override fun updatePartner(dto: PartnerUpdateDto): Either<ServiceError, Partner> = transaction {
        getPartnerById(dto.id).flatMap { partner ->
            partnerRepository.update(dto).flatMap { newPartner ->
                newPartner.right() //keycloakGroupIntegration.updateGroup(partner.navn, newPartner.navn)
                    .bimap({ rollback(); it }, { newPartner })
            }
        }
    }

    //TODO: Handle Keycloak logic: Should probably be the same as delete.
    override fun archiveOne(id: UUID): Either<ServiceError, Unit> {
        return transaction { partnerRepository.archiveOne(id)
            .map{
                    kontaktService.getKontakter(KontaktGetDto(aktorId = id))
                        .map {
                            it.map { kontakt ->
                                kontaktService.deleteKontaktById(kontakt.id)
                            }
                                .sequence(Either.applicative())
                                .flatMap { Either.Right(Unit) }
                        }
                }
            .flatMap { it.flatMap { it } }
            .fold({rollback(); it.left()}, { it.right()})
        }
    }
}