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
import ombruk.backend.henting.domain.entity.PlanlagtHentingWithParents
import ombruk.backend.henting.domain.port.IPlanlagtHentingRepository
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
class PlanlagtHentingService(val planlagtHentingRepository: IPlanlagtHentingRepository): IPlanlagtHentingService {
    override fun create(dto: PlanlagtHentingSaveDto): Either<ServiceError, PlanlagtHentingWithParents> {
        return transaction { planlagtHentingRepository.insert(dto) }
    }

    override fun findOne(id: UUID): Either<ServiceError, PlanlagtHentingWithParents> {
        return transaction { planlagtHentingRepository.findOne(id) }
    }

    override fun find(dto: PlanlagtHentingFindDto): Either<ServiceError, List<PlanlagtHentingWithParents>> {
        return transaction { planlagtHentingRepository.find(dto) }
    }

    override fun delete(dto: PlanlagtHentingDeleteDto): Either<ServiceError, Unit> {
        return transaction { planlagtHentingRepository.delete(dto.id) }
    }

    override fun update(dto: PlanlagtHentingUpdateDto): Either<ServiceError, PlanlagtHentingWithParents> {
        return transaction { planlagtHentingRepository.update(dto) }
    }

    override fun batchCreateForHenteplan(dto: PlanlagtHentingBatchPostDto): Either<ServiceError, List<PlanlagtHentingWithParents>> {
        return transaction {
            dto.dateList.map {
                planlagtHentingRepository.insert(
                    PlanlagtHentingSaveDto(
                        henteplanId = dto.saveDto.henteplanId,
                        merknad = dto.saveDto.merknad,
                        startTidspunkt = LocalDateTime.of(it, dto.saveDto.startTidspunkt.toLocalTime()),
                        sluttTidspunkt = LocalDateTime.of(it, dto.saveDto.sluttTidspunkt.toLocalTime()),
                    ))
            }
                .sequence(Either.applicative())
                .fix()
                .map { it.fix() }
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

    override fun findWithParents(dto: PlanlagtHentingFindDto): Either<ServiceError, List<PlanlagtHentingWithParents>> {
        return transaction { planlagtHentingRepository.findWithParents(dto) }
    }

}