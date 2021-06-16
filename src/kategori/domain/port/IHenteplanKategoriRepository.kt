package ombruk.backend.kategori.domain.port

import arrow.core.Either
import ombruk.backend.kategori.domain.entity.HenteplanKategori
import ombruk.backend.kategori.domain.params.HenteplanKategoriCreateParams
import ombruk.backend.kategori.domain.params.HenteplanKategoriFindParams
import ombruk.backend.shared.error.RepositoryError
import java.util.*

interface IHenteplanKategoriRepository {
    fun insert(params: HenteplanKategoriCreateParams): Either<RepositoryError, HenteplanKategori>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun find(params: HenteplanKategoriFindParams): Either<RepositoryError, List<HenteplanKategori>>
    fun archive(params: HenteplanKategoriFindParams): Either<RepositoryError, List<HenteplanKategori>>
    fun archiveOne(id: UUID): Either<RepositoryError, HenteplanKategori>
}