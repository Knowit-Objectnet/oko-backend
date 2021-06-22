package ombruk.backend.utlysning.application.service

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.utlysning.application.api.dto.*
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.domain.params.UtlysningFindParams
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

            utlysningRepository.find(dto)
                .fold(
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
            dto.partnerIds
                .filter {
                    val find = utlysningRepository
                        .find(UtlysningFindDto(partnerId = UUID.fromString(it), hentingId = dto.hentingId))
                    find is Either.Right && find.b.isEmpty()
                }
                .map {
                utlysningRepository.insert(
                    UtlysningSaveDto(
                        partnerId = UUID.fromString(it),
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

    override fun partnerAccept(dtoPartner: UtlysningPartnerAcceptDto): Either<ServiceError, Utlysning> {
        return transaction {
            utlysningRepository.acceptPartner(dtoPartner)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun stasjonAccept(dtoPartner: UtlysningStasjonAcceptDto): Either<ServiceError, Utlysning> {
        return transaction {
            utlysningRepository.acceptStasjon(dtoPartner)
                .fold({ rollback(); it.left() }, { it.right() })
        }
    }

    override fun findAccepted(ekstraHentingId: UUID): Either<ServiceError, Utlysning?> {
        return transaction {
            utlysningRepository.find(UtlysningFindDto(hentingId = ekstraHentingId, partnerPameldt = true))
                .map { list -> if (list.isNotEmpty()) list.sortedBy { it.partnerPameldt }[0] else null }
        }
    }

    override fun archive(params: UtlysningFindParams): Either<ServiceError, Unit> {
        return transaction {
            utlysningRepository.archive(params)
                .fold(
                    {Either.Left(ServiceError(it.message))},
                    {Either.Right(Unit)}
                )
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

    override fun archiveOne(id: UUID): Either<ServiceError, Unit> {
        return transaction {
            utlysningRepository.archiveOne(id)
                .fold(
                    {Either.Left(ServiceError(it.message))},
                    {Either.Right(Unit)}
                )
                .fold({rollback(); it.left()}, {it.right()})
        }
    }
}