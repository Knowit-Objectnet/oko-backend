package ombruk.backend.henting.domain.port

import arrow.core.Either
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.params.EkstraHentingCreateParams
import ombruk.backend.henting.domain.params.EkstraHentingFindParams
import ombruk.backend.henting.domain.params.EkstraHentingUpdateParams
import ombruk.backend.shared.error.RepositoryError
import java.util.*

interface IEkstraHentingRepository {
    fun insert(params: EkstraHentingCreateParams): Either<RepositoryError, EkstraHenting>
    fun update(params: EkstraHentingUpdateParams): Either<RepositoryError, EkstraHenting>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun findOne(id: UUID): Either<RepositoryError, EkstraHenting>
    fun find(params: EkstraHentingFindParams): Either<RepositoryError, List<EkstraHenting>>
}