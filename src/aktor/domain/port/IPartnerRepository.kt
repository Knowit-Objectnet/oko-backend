package ombruk.backend.aktor.domain.port

import arrow.core.Either
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.model.PartnerCreateParams
import ombruk.backend.aktor.domain.model.PartnerFindParams
import ombruk.backend.aktor.domain.model.PartnerUpdateParams
import ombruk.backend.shared.error.RepositoryError

interface IPartnerRepository {
    fun insert(params: PartnerCreateParams): Either<RepositoryError, Partner>
    fun update(params: PartnerUpdateParams): Either<RepositoryError, Partner>
    fun delete(id: Int): Either<RepositoryError, Unit>
    fun findOne(id: Int): Either<RepositoryError, Partner>
    fun find(params: PartnerFindParams): Either<RepositoryError, List<Partner>>
}