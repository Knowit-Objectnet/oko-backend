package ombruk.backend.avtale.domain.port

import arrow.core.Either
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.domain.model.AvtaleCreateParams
import ombruk.backend.avtale.domain.model.AvtaleFindParams
import ombruk.backend.avtale.domain.model.AvtaleUpdateParams
import ombruk.backend.shared.error.RepositoryError

interface IAvtaleRepository {
    fun insert(params: AvtaleCreateParams): Either<RepositoryError, Avtale>
    fun update(params: AvtaleUpdateParams): Either<RepositoryError, Avtale>
    fun delete(id: Int): Either<RepositoryError, Unit>
    fun findOne(id: Int): Either<RepositoryError, Avtale>
    fun find(params: AvtaleFindParams): Either<RepositoryError, List<Avtale>>
}