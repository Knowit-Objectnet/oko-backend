package ombruk.backend.henting.domain.port

import arrow.core.Either
import ombruk.backend.henting.domain.params.HenteplanCreateParams
import ombruk.backend.henting.domain.params.HenteplanFindParams
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.shared.error.RepositoryError

interface IHenteplanRepository {
    fun insert(params: HenteplanCreateParams): Either<RepositoryError, Henteplan>
    //    fun update(params: PartnerUpdateParams): Either<RepositoryError, Partner>
    fun delete(id: Int): Either<RepositoryError, Unit>
    fun findOne(id: Int): Either<RepositoryError, Henteplan>
    fun find(params: HenteplanFindParams): Either<RepositoryError, List<Henteplan>>
}