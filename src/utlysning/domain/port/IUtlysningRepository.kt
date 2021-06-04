package ombruk.backend.utlysning.domain.port

import arrow.core.Either
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.domain.params.UtlysningCreateParams
import ombruk.backend.utlysning.domain.params.UtlysningFindParams
import ombruk.backend.utlysning.domain.params.UtlysningUpdateParams
import java.util.*

interface IUtlysningRepository {
    fun insert(params: UtlysningCreateParams): Either<RepositoryError, Utlysning>
    fun update(params: UtlysningUpdateParams): Either<RepositoryError, Utlysning>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun findOne(id: UUID): Either<RepositoryError, Utlysning>
    fun find(params: UtlysningFindParams): Either<RepositoryError, List<Utlysning>>
}