package ombruk.backend.henting.application.service

import arrow.core.Either
import ombruk.backend.henting.application.api.dto.*
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.params.PlanlagtHentingFindParams
import ombruk.backend.shared.error.ServiceError
import java.time.LocalDateTime
import java.util.*

interface IPlanlagtHentingService {

    fun save(dto: PlanlagtHentingSaveDto): Either<ServiceError, PlanlagtHenting>

    fun findOne(id: UUID): Either<ServiceError, PlanlagtHenting>

    fun find(dto: PlanlagtHentingFindDto): Either<ServiceError, List<PlanlagtHenting>>

    fun delete(dto: PlanlagtHentingDeleteDto): Either<ServiceError, Unit>

    fun update(dto: PlanlagtHentingUpdateDto): Either<ServiceError, PlanlagtHenting>
    fun update(dto: PlanlagtHentingUpdateDto, avlystAv: UUID): Either<ServiceError, PlanlagtHenting>

    fun batchSaveForHenteplan(dto: PlanlagtHentingBatchPostDto): Either<ServiceError, List<PlanlagtHenting>>

    fun archiveOne(id: UUID): Either<ServiceError, Unit>

    fun archive(params: PlanlagtHentingFindParams): Either<ServiceError, Unit>

    fun updateAvlystDate(id: UUID, date: LocalDateTime, aarsakId: UUID, avlystAv: UUID): Either<ServiceError, PlanlagtHenting>

}