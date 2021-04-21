package ombruk.backend.aktor.domain.port

import arrow.core.Either
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.shared.error.RepositoryError

interface IKontaktRepository {
    /*fun save(params: KontaktCreateParams): Either<RepositoryError, KontaktPerson>
    fun update(params: KontaktUpdateParams): Either<RepositoryError, KontaktPerson>
    fun delete(id: Int): Either<RepositoryError, Unit>
    fun findOne(id: Int): Either<RepositoryError, KontaktPerson>
    fun find(params: KontaktFindParams): Either<RepositoryError, List<KontaktPerson>>*/
    fun findForAktor(id: Int): Either<RepositoryError, List<Kontakt>>
}