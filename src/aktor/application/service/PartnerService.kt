package ombruk.backend.aktor.application.service

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.list.traverse.sequence
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import io.ktor.locations.*
import io.ktor.util.*
import ombruk.backend.aktor.application.api.dto.KontaktGetDto
import ombruk.backend.aktor.application.api.dto.PartnerGetDto
import ombruk.backend.aktor.application.api.dto.PartnerSaveDto
import ombruk.backend.aktor.application.api.dto.PartnerUpdateDto
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.port.IPartnerRepository
import ombruk.backend.avtale.application.api.dto.AvtaleFindDto
import ombruk.backend.avtale.application.service.IAvtaleService
import ombruk.backend.shared.api.KeycloakGroupIntegration
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.utlysning.application.api.dto.UtlysningFindDto
import ombruk.backend.utlysning.application.service.IUtlysningService
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class PartnerService constructor(
    private val keycloakGroupIntegration: KeycloakGroupIntegration,
    private val partnerRepository: IPartnerRepository,
    private val kontaktService: IKontaktService,
    private val avtaleService: IAvtaleService,
    private val utlysningService: IUtlysningService
) : IPartnerService {

    @KtorExperimentalAPI
    override fun savePartner(dto: PartnerSaveDto): Either<ServiceError, Partner> = transaction {
        partnerRepository.insert(dto).flatMap { partner ->
            keycloakGroupIntegration.createGroup(partner.navn, partner.id)
                .bimap({ rollback(); it }, { partner })
        }
    }

    override fun getPartnerById(id: UUID, includeKontakt: Boolean): Either<ServiceError, Partner> {
        return transaction {
            partnerRepository.findOne(id)
                .flatMap { partner ->
                    if (includeKontakt) kontaktService.getKontakter(KontaktGetDto(aktorId = partner.id))
                        .flatMap { kontakter -> partner.copy(kontaktPersoner = kontakter).right() }
                    else partner.right()
                }
        }
    }

    @KtorExperimentalLocationsAPI
    fun getPartnereWithAvtaler(dto: PartnerGetDto, includeKontakt: Boolean): Either<ServiceError, List<Partner>> {
        return transaction {
            partnerRepository.find(dto)
                .flatMap {
                    it.map { partner ->
                        avtaleService.find(AvtaleFindDto(aktorId = partner.id))
                            .flatMap { avtaler ->
                                if (includeKontakt) kontaktService.getKontakter(KontaktGetDto(aktorId = partner.id))
                                    .flatMap { kontakter -> partner.copy(kontaktPersoner = kontakter, avtaler = avtaler).right() }
                                else partner.copy(avtaler = avtaler).right()
                            }
                    }.sequence(Either.applicative()).fix().map { it.fix() }
                }
        }
    }

    @KtorExperimentalLocationsAPI
    override fun getPartnere(dto: PartnerGetDto, includeKontakt: Boolean): Either<ServiceError, List<Partner>> {
        if (dto.includeAvtaler) return getPartnereWithAvtaler(dto, includeKontakt)
        return transaction {
            partnerRepository.find(dto)
                .flatMap {
                    it.map { partner ->
                        if (includeKontakt) kontaktService.getKontakter(KontaktGetDto(aktorId = partner.id))
                            .flatMap { kontakter -> partner.copy(kontaktPersoner = kontakter).right() }
                        else partner.right()
                    }.sequence(Either.applicative()).fix().map { it.fix() }
                }
        }
    }

    @KtorExperimentalAPI
    override fun deletePartnerById(id: UUID): Either<ServiceError, Partner> = transaction {
        getPartnerById(id, false).flatMap { partner ->
            partnerRepository.delete(id)
                .flatMap { keycloakGroupIntegration.deleteGroup(partner.navn) }
                .bimap({ rollback(); it }, { partner })
        }
    }


    @KtorExperimentalAPI
    override fun updatePartner(dto: PartnerUpdateDto): Either<ServiceError, Partner> = transaction {
        getPartnerById(dto.id, false).flatMap { partner ->
            partnerRepository.update(dto).flatMap { newPartner ->
                keycloakGroupIntegration.updateGroup(partner.navn, newPartner.navn)
                    .bimap({ rollback(); it }, { newPartner })
            }
        }
    }

    override fun archiveOne(id: UUID): Either<ServiceError, Unit> {
        return transaction { partnerRepository.archiveOne(id)
            .map{ partner ->
                    kontaktService.getKontakter(KontaktGetDto(aktorId = id))
                        .map {
                            it.map { kontakt ->
                                kontaktService.deleteKontaktById(kontakt.id)
                            }
                                .sequence(Either.applicative())
                                .flatMap { Either.Right(Unit) }
                        }
                        .flatMap { it }
                        .map { avtaleService.archive(AvtaleFindDto(aktorId = partner.id)) }.flatMap { it }
                        .map { utlysningService.archive(UtlysningFindDto(partnerId = partner.id)) }.flatMap { it }
                        .map { keycloakGroupIntegration.deleteGroup(partner.navn) }.map { Unit }
                }
            .flatMap { it }
            .fold({rollback(); it.left()}, { it.right()})
        }
    }
}