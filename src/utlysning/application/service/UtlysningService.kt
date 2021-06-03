package ombruk.backend.utlysning.application.service

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.utlysning.application.api.dto.UtlysningBatchSaveDto
import ombruk.backend.utlysning.application.api.dto.UtlysningDeleteDto
import ombruk.backend.utlysning.application.api.dto.UtlysningFindDto
import ombruk.backend.utlysning.application.api.dto.UtlysningSaveDto
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.domain.port.IUtlysningRepository
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class UtlysningService(val utlysningRepository: IUtlysningRepository) : IUtlysningService {
    override fun save(dto: UtlysningSaveDto): Either<ServiceError, Utlysning> {
        return transaction {
            utlysningRepository.insert(dto)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun findOne(id: UUID): Either<ServiceError, Utlysning> {
        return transaction {
            utlysningRepository.findOne(id)
        }
    }

    override fun find(dto: UtlysningFindDto): Either<ServiceError, List<Utlysning>> {

        return transaction {

            val utlysnigner = utlysningRepository.find(dto)
            utlysnigner.fold(
                { Either.Left(ServiceError(it.message)) },
                { it.right() }
            )

        }
    }

    override fun delete(dto: UtlysningDeleteDto): Either<ServiceError, Unit> {
        return transaction {
            utlysningRepository.delete(dto.id)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun batchSave(dto: UtlysningBatchSaveDto): Either<ServiceError, List<Utlysning>> {
        return transaction {
            dto.partnerIds.map {
                utlysningRepository.insert(
                    UtlysningSaveDto(
                        partnerId = it,
                        hentingId = dto.hentingId,
                        partnerPameldt = dto.partnerPameldt,
                        stasjonGodkjent = dto.stasjonGodkjent,
                        partnerSkjult = dto.partnerSkjult,
                        partnerVist = dto.partnerVist
                    )
                )
            }
                .sequence(Either.applicative())
                .fix()
                .map { it.fix() }
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

}