package ombruk.backend.henting.domain.port

import arrow.core.Either
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.params.*
import ombruk.backend.shared.error.RepositoryError
import java.util.*

interface IPlanlagtHentingRepository {
    fun insert(params: PlanlagtHentingCreateParams): Either<RepositoryError, PlanlagtHenting>
    fun update(params: PlanlagtHentingUpdateParams): Either<RepositoryError, PlanlagtHenting>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun findOne(id: UUID): Either<RepositoryError, PlanlagtHenting>
    fun find(params: PlanlagtHentingFindParams): Either<RepositoryError, List<PlanlagtHenting>>

}