package ombruk.backend.aktor.domain.port

import arrow.core.Either
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.model.*
import ombruk.backend.shared.error.RepositoryError
import java.util.*

interface IKontaktRepository {
    fun find(params: KontaktFindParams): Either<RepositoryError, List<Kontakt>>
    fun findOne(id: UUID): Either<RepositoryError, Kontakt>
    fun insert(params: KontaktCreateParams): Either<RepositoryError, Kontakt>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun update(params: KontaktUpdateParams): Either<RepositoryError, Kontakt>
}