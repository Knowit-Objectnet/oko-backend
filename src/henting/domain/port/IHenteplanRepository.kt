package ombruk.backend.henting.domain.port

import arrow.core.Either
import ombruk.backend.henting.domain.params.HenteplanCreateParams
import ombruk.backend.henting.domain.params.HenteplanFindParams
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.params.HenteplanUpdateParams
import ombruk.backend.shared.error.RepositoryError
import java.util.*

interface IHenteplanRepository {
    fun insert(params: HenteplanCreateParams): Either<RepositoryError, Henteplan>
    fun update(params: HenteplanUpdateParams): Either<RepositoryError, Henteplan>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun findOne(id: UUID): Either<RepositoryError, Henteplan>
    fun find(params: HenteplanFindParams): Either<RepositoryError, List<Henteplan>>
}