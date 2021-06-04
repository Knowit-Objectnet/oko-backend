package ombruk.backend.aktor.domain.port

import arrow.core.Either
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.model.PartnerCreateParams
import ombruk.backend.aktor.domain.model.PartnerFindParams
import ombruk.backend.aktor.domain.model.PartnerUpdateParams
import ombruk.backend.shared.error.RepositoryError
import java.util.*

interface IPartnerRepository {
    fun insert(params: PartnerCreateParams): Either<RepositoryError, Partner>
    fun update(params: PartnerUpdateParams): Either<RepositoryError, Partner>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun findOne(id: UUID): Either<RepositoryError, Partner>
    fun find(params: PartnerFindParams): Either<RepositoryError, List<Partner>>
}