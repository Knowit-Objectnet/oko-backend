package ombruk.backend.avtale.domain.port

import arrow.core.Either
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.domain.params.AvtaleCreateParams
import ombruk.backend.avtale.domain.params.AvtaleFindParams
import ombruk.backend.avtale.domain.params.AvtaleUpdateParams
import ombruk.backend.shared.error.RepositoryError
import java.util.*

interface IAvtaleRepository {
    fun insert(params: AvtaleCreateParams): Either<RepositoryError, Avtale>
    fun update(params: AvtaleUpdateParams): Either<RepositoryError, Avtale>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun findOne(id: UUID): Either<RepositoryError, Avtale>
    fun find(params: AvtaleFindParams): Either<RepositoryError, List<Avtale>>
    fun archiveOne(id: UUID): Either<RepositoryError, Avtale>
    fun archive(params: AvtaleFindParams): Either<RepositoryError, List<Avtale>>
}