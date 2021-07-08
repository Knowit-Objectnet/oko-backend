package ombruk.backend.aarsak.application.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.aarsak.application.api.dto.AarsakFindDto
import ombruk.backend.aarsak.application.api.dto.AarsakSaveDto
import ombruk.backend.aarsak.application.api.dto.AarsakUpdateDto
import ombruk.backend.aarsak.domain.entity.Aarsak
import ombruk.backend.aarsak.domain.port.IAarsakRepository
import ombruk.backend.shared.api.KeycloakGroupIntegration
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class AarsakService(
    private val aarsakRepository: IAarsakRepository,
) : IAarsakService {
    override fun findOne(id: UUID): Either<ServiceError, Aarsak> {
        return transaction {
            aarsakRepository.findOne(id)
        }
    }

    override fun find(dto: AarsakFindDto): Either<ServiceError, List<Aarsak>> {
        return transaction {
            aarsakRepository.find(dto).fold(
                { Either.Left(ServiceError(it.message)) },
                { it.right() }
            )
        }
    }

    override fun save(dto: AarsakSaveDto): Either<ServiceError, Aarsak> {
        return transaction {
            aarsakRepository.insert(dto)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun delete(id: UUID): Either<ServiceError, Aarsak> {
        TODO("Not yet implemented")
    }

    override fun update(dto: AarsakUpdateDto): Either<ServiceError, Aarsak> {
        return transaction {
            aarsakRepository.update(dto)
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

    override fun archiveOne(id: UUID): Either<ServiceError, Unit> {
        return transaction {
            aarsakRepository.archiveOne(id)
                .fold({rollback(); it.left()}, { Either.right(Unit)})
        }
    }
}