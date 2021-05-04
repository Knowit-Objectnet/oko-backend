package ombruk.backend.avtale.domain.port

import arrow.core.Either
import ombruk.backend.avtale.domain.entity.Henteplan
import ombruk.backend.avtale.domain.model.*
import ombruk.backend.shared.error.RepositoryError

interface IHenteplanRepository {
    fun insert(params: HenteplanCreateParams): Either<RepositoryError, Henteplan>
    fun update(params: HenteplanUpdateParams): Either<RepositoryError, Henteplan>
    fun delete(id: Int): Either<RepositoryError, Unit>
    fun findOne(id: Int): Either<RepositoryError, Henteplan>
    fun find(params: HenteplanFindParams): Either<RepositoryError, List<Henteplan>>
}