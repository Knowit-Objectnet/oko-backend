package ombruk.backend.henting.application.service

import arrow.core.Either
import ombruk.backend.henting.application.api.dto.PlanlagtHentingDeleteDto
import ombruk.backend.henting.application.api.dto.PlanlagtHentingFindDto
import ombruk.backend.henting.application.api.dto.PlanlagtHentingPostDto
import ombruk.backend.henting.application.api.dto.PlanlagtHentingUpdateDto
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.shared.error.ServiceError
import java.util.*

interface IPlanlagtHentingService {

    fun create(dto: PlanlagtHentingPostDto): Either<ServiceError, PlanlagtHenting>

    fun findOne(id: UUID): Either<ServiceError, PlanlagtHenting>

    fun find(dto: PlanlagtHentingFindDto): Either<ServiceError, List<PlanlagtHenting>>

    fun delete(dto: PlanlagtHentingDeleteDto): Either<ServiceError, Unit>

    fun update(dto: PlanlagtHentingUpdateDto): Either<ServiceError, PlanlagtHenting>
}