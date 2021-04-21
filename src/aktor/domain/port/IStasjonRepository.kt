package ombruk.backend.aktor.domain.port

import arrow.core.Either
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.model.StasjonCreateParams
import ombruk.backend.aktor.domain.model.StasjonFindParams
import ombruk.backend.aktor.domain.model.StasjonUpdateParams
import ombruk.backend.shared.error.RepositoryError

interface IStasjonRepository {
    fun find(params: StasjonFindParams): Either<RepositoryError, List<Stasjon>>
    fun findOne(id: Int): Either<RepositoryError, Stasjon>
    fun create(params: StasjonCreateParams): Either<RepositoryError, Stasjon>
    fun delete(id: Int): Either<RepositoryError, Unit>
    fun update(params: StasjonUpdateParams): Either<RepositoryError, Stasjon>
}