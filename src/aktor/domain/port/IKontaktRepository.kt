package ombruk.backend.aktor.domain.port

import arrow.core.Either
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.model.KontaktCreateParams
import ombruk.backend.aktor.domain.model.StasjonCreateParams
import ombruk.backend.aktor.domain.model.StasjonFindParams
import ombruk.backend.aktor.domain.model.StasjonUpdateParams
import ombruk.backend.shared.error.RepositoryError

interface IKontaktRepository {
    fun find(params: StasjonFindParams): Either<RepositoryError, List<Kontakt>>
    fun findOne(id: Int): Either<RepositoryError, Kontakt>
    fun insert(params: StasjonCreateParams): Either<RepositoryError, Kontakt>
    fun delete(id: Int): Either<RepositoryError, Unit>
    fun update(params: StasjonUpdateParams): Either<RepositoryError, Kontakt>
}