package ombruk.backend.kategori.domain.port

import arrow.core.Either
import ombruk.backend.kategori.domain.entity.EkstraHentingKategori
import ombruk.backend.kategori.domain.params.EkstraHentingKategoriCreateParams
import ombruk.backend.kategori.domain.params.EkstraHentingKategoriFindParams
import ombruk.backend.shared.error.RepositoryError
import java.util.*

interface IEkstraHentingKategoriRepository {
    fun insert(params: EkstraHentingKategoriCreateParams): Either<RepositoryError, EkstraHentingKategori>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun find(params: EkstraHentingKategoriFindParams): Either<RepositoryError, List<EkstraHentingKategori>>
    fun archive(params: EkstraHentingKategoriFindParams): Either<RepositoryError, List<EkstraHentingKategori>>
    fun archiveOne(id: UUID): Either<RepositoryError, EkstraHentingKategori>
}