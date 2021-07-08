package ombruk.backend.aarsak.domain.port

import arrow.core.Either
import ombruk.backend.aarsak.domain.entity.Aarsak
import ombruk.backend.aarsak.domain.model.AarsakCreateParams
import ombruk.backend.aarsak.domain.model.AarsakFindParams
import ombruk.backend.aarsak.domain.model.AarsakUpdateParams
import ombruk.backend.shared.error.RepositoryError
import java.util.*

interface IAarsakRepository {
    fun find(params: AarsakFindParams): Either<RepositoryError, List<Aarsak>>
    fun findOne(id: UUID): Either<RepositoryError, Aarsak>
    fun insert(params: AarsakCreateParams): Either<RepositoryError, Aarsak>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun update(params: AarsakUpdateParams): Either<RepositoryError, Aarsak>
}