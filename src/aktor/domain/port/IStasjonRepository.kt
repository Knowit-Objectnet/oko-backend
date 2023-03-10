package ombruk.backend.aktor.domain.port

import arrow.core.Either
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.model.StasjonCreateParams
import ombruk.backend.aktor.domain.model.StasjonFindParams
import ombruk.backend.aktor.domain.model.StasjonUpdateParams
import ombruk.backend.shared.error.RepositoryError
import java.util.*

interface IStasjonRepository {
    fun find(params: StasjonFindParams): Either<RepositoryError, List<Stasjon>>
    fun findOne(id: UUID): Either<RepositoryError, Stasjon>
    fun insert(params: StasjonCreateParams): Either<RepositoryError, Stasjon>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun update(params: StasjonUpdateParams): Either<RepositoryError, Stasjon>
    fun archiveOne(id: UUID): Either<RepositoryError, Stasjon>
}