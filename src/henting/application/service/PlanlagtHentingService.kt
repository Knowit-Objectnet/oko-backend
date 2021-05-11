package ombruk.backend.henting.application.service

import arrow.core.Either
import io.ktor.locations.*
import ombruk.backend.henting.application.api.dto.PlanlagtHentingDeleteDto
import ombruk.backend.henting.application.api.dto.PlanlagtHentingFindDto
import ombruk.backend.henting.application.api.dto.PlanlagtHentingPostDto
import ombruk.backend.henting.application.api.dto.PlanlagtHentingUpdateDto
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.port.IPlanlagtHentingRepository
import ombruk.backend.henting.infrastructure.repository.PlanlagtHentingRepository
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
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
}