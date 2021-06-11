package ombruk.backend.aktor.application.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import ombruk.backend.aktor.application.api.dto.StasjonSaveDto
import ombruk.backend.aktor.application.api.dto.StasjonFindDto
import ombruk.backend.aktor.application.api.dto.StasjonUpdateDto
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.shared.api.KeycloakGroupIntegration
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.reflect.TypeVariable
import java.util.*

class StasjonService(
    val stasjonRepository: IStasjonRepository,
    val keycloakGroupIntegration: KeycloakGroupIntegration
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
        return transaction { stasjonRepository.findOne(id) }
    }

    override fun find(dto: StasjonFindDto): Either<ServiceError, List<Stasjon>> {
        return transaction { stasjonRepository.find((dto)) }
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
            .fold({rollback(); it.left()}, { Either.right(Unit)})
        }
    }
}