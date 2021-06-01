package ombruk.backend.henting.domain.port

import arrow.core.Either
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.entity.PlanlagtHentingWithParents
import ombruk.backend.henting.domain.params.*
import ombruk.backend.shared.error.RepositoryError
import java.util.*

interface IPlanlagtHentingRepository {
    fun insert(params: PlanlagtHentingCreateParams): Either<RepositoryError, PlanlagtHentingWithParents>
    fun update(params: PlanlagtHentingUpdateParams): Either<RepositoryError, PlanlagtHentingWithParents>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun findOne(id: UUID): Either<RepositoryError, PlanlagtHentingWithParents>
    fun find(params: PlanlagtHentingFindParams): Either<RepositoryError, List<PlanlagtHentingWithParents>>
}