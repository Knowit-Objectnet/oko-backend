package ombruk.backend.henting.application.service

import arrow.core.Either
import ombruk.backend.henting.application.api.dto.*
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.entity.PlanlagtHentingWithParents
import ombruk.backend.shared.error.ServiceError
import java.util.*

interface IPlanlagtHentingService {

    fun save(dto: PlanlagtHentingSaveDto): Either<ServiceError, PlanlagtHentingWithParents>

    fun findOne(id: UUID): Either<ServiceError, PlanlagtHentingWithParents>

    fun find(dto: PlanlagtHentingFindDto): Either<ServiceError, List<PlanlagtHentingWithParents>>

    fun delete(dto: PlanlagtHentingDeleteDto): Either<ServiceError, Unit>

    fun update(dto: PlanlagtHentingUpdateDto): Either<ServiceError, PlanlagtHentingWithParents>

    fun batchSaveForHenteplan(dto: PlanlagtHentingBatchPostDto): Either<ServiceError, List<PlanlagtHentingWithParents>>

}