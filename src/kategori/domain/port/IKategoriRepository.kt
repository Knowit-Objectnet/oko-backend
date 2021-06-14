package ombruk.backend.kategori.domain.port

import arrow.core.Either
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.kategori.domain.params.KategoriCreateParams
import ombruk.backend.kategori.domain.params.KategoriFindParams
import ombruk.backend.shared.error.RepositoryError
import java.util.*

interface IKategoriRepository {
    fun insert(params: KategoriCreateParams): Either<RepositoryError, Kategori>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun findOne(id: UUID): Either<RepositoryError, Kategori>
    fun find(params: KategoriFindParams): Either<RepositoryError, List<Kategori>>
    fun archive(params: KategoriFindParams): Either<RepositoryError, List<Kategori>>
    fun archiveOne(id: UUID): Either<RepositoryError, Kategori>
}