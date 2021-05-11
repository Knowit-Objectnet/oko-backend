package ombruk.backend.henting.application.service

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import io.ktor.locations.*
import ombruk.backend.henting.application.api.dto.*
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.port.IPlanlagtHentingRepository
import ombruk.backend.henting.infrastructure.repository.PlanlagtHentingRepository
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
class PlanlagtHentingService(val planlagtHentingRepository: IPlanlagtHentingRepository): IPlanlagtHentingService {
    override fun create(dto: PlanlagtHentingPostDto): Either<ServiceError, PlanlagtHenting> {
        return transaction { planlagtHentingRepository.insert(dto) }
    }

    override fun findOne(id: UUID): Either<ServiceError, PlanlagtHenting> {
        return transaction { planlagtHentingRepository.findOne(id) }
    }

    override fun find(dto: PlanlagtHentingFindDto): Either<ServiceError, List<PlanlagtHenting>> {
        return transaction { planlagtHentingRepository.find(dto) }
    }

    override fun delete(dto: PlanlagtHentingDeleteDto): Either<ServiceError, Unit> {
        return transaction { planlagtHentingRepository.delete(dto.id) }
    }

    override fun update(dto: PlanlagtHentingUpdateDto): Either<ServiceError, PlanlagtHenting> {
        return transaction { planlagtHentingRepository.update(dto) }
    }

    override fun batchCreateForHenteplan(dto: PlanlagtHentingBatchPostDto): Either<ServiceError, List<PlanlagtHenting>> {
        return transaction {
            dto.dateList.map {
                planlagtHentingRepository.insert(
                    PlanlagtHentingPostDto(
                        henteplanId = dto.postDto.henteplanId,
                        merknad = dto.postDto.merknad,
                        startTidspunkt = LocalDateTime.of(it, dto.postDto.startTidspunkt.toLocalTime()),
                        sluttTidspunkt = LocalDateTime.of(it, dto.postDto.sluttTidspunkt.toLocalTime()),
                    ))
            }
                .sequence(Either.applicative())
                .fix()
                .map { it.fix() }
                .fold({rollback(); it.left()}, {it.right()})
        }
    }
}