package ombruk.backend.aktor.application.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import arrow.core.flatMap
import ombruk.backend.aktor.application.api.dto.KontaktGetDto
import ombruk.backend.aktor.application.api.dto.StasjonFindDto
import ombruk.backend.aktor.application.api.dto.StasjonSaveDto
import ombruk.backend.aktor.application.api.dto.StasjonUpdateDto
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.avtale.application.api.dto.AvtaleFindDto
import ombruk.backend.avtale.application.service.IAvtaleService
import ombruk.backend.henting.application.api.dto.EkstraHentingFindDto
import ombruk.backend.henting.application.api.dto.HenteplanFindDto
import ombruk.backend.henting.application.service.IEkstraHentingService
import ombruk.backend.henting.application.service.IHenteplanService
import ombruk.backend.shared.api.KeycloakGroupIntegration
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class StasjonService(
    val stasjonRepository: IStasjonRepository,
    val keycloakGroupIntegration: KeycloakGroupIntegration,
    val kontaktService: IKontaktService,
    val henteplanService: IHenteplanService,
    val avtaleService: IAvtaleService,
    val ekstraHentingService: IEkstraHentingService
) :
    IStasjonService {

    override fun save(dto: StasjonSaveDto): Either<ServiceError, Stasjon> {
        return transaction {
            stasjonRepository.insert(dto).flatMap { stasjon ->
                stasjon.right() //keycloakGroupIntegration.createGroup(stasjon.navn, stasjon.id)
                    .bimap({ rollback(); it }, { stasjon })
            }
        }
    }

    override fun findOne(id: UUID): Either<ServiceError, Stasjon> {
        return transaction {
            stasjonRepository.findOne(id)
                .flatMap { stasjon ->
                    kontaktService.getKontakter(KontaktGetDto(aktorId = stasjon.id))
                        .flatMap { kontakter -> stasjon.copy(kontaktPersoner = kontakter).right() }
                }
        }
    }

    override fun find(dto: StasjonFindDto): Either<ServiceError, List<Stasjon>> {
        return transaction {
            stasjonRepository.find((dto))
                .flatMap {
                    it.map { stasjon ->
                        kontaktService.getKontakter(KontaktGetDto(aktorId = stasjon.id))
                            .flatMap { kontakter -> stasjon.copy(kontaktPersoner = kontakter).right() }
                    }.sequence(Either.applicative()).fix().map { it.fix() }
                }
        }
    }

    override fun delete(id: UUID): Either<ServiceError, Stasjon> {
        return transaction {
            findOne(id).flatMap { stasjon ->
                stasjonRepository.delete(id)
                    //.flatMap { keycloakGroupIntegration.deleteGroup(stasjon.navn) }
                    .bimap({ rollback(); it }, { stasjon })
            }
        }
    }

    override fun update(dto: StasjonUpdateDto): Either<ServiceError, Stasjon> = transaction {
        findOne(dto.id).flatMap { stasjon ->
            stasjonRepository.update(dto).flatMap { newStasjon ->
                newStasjon.right() //keycloakGroupIntegration.updateGroup(stasjon.navn, newStasjon.navn)
                    .bimap({ rollback(); it }, { newStasjon })
            }
        }
    }

    //TODO: Handle Keycloak logic: Should probably be the same as delete.
    override fun archiveOne(id: UUID): Either<ServiceError, Unit> {
        return transaction { stasjonRepository.archiveOne(id)
            .map{ stasjon ->
                kontaktService.getKontakter(KontaktGetDto(aktorId = id))
                    .map {
                        it.map { kontakt ->
                            kontaktService.deleteKontaktById(kontakt.id)
                        }
                            .sequence(Either.applicative())
                            .flatMap { Either.Right(Unit) }
                    }
                    .flatMap { it }
                    .map { henteplanService.archive(HenteplanFindDto(stasjonId = stasjon.id)) }.flatMap { it }
                    .map { avtaleService.archive(AvtaleFindDto(aktorId = stasjon.id)) }.flatMap { it }
                    .map {
                        ekstraHentingService.archive(
                            EkstraHentingFindDto(stasjonId = stasjon.id, after = LocalDateTime.now())
                        )
                    }.flatMap { it }
            }
            .flatMap { it }
            .fold({rollback(); it.left()}, { it.right()})
        }
    }
}