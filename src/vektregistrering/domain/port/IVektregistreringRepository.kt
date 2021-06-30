package ombruk.backend.vektregistrering.domain.port

import arrow.core.Either
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.kategori.domain.params.KategoriCreateParams
import ombruk.backend.kategori.domain.params.KategoriFindParams
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.vektregistrering.domain.entity.Vektregistrering
import ombruk.backend.vektregistrering.domain.params.VektregistreringCreateParams
import ombruk.backend.vektregistrering.domain.params.VektregistreringFindParams
import java.util.*

interface IVektregistreringRepository {
    fun insert(params: VektregistreringCreateParams): Either<RepositoryError, Vektregistrering>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun findOne(id: UUID): Either<RepositoryError, Vektregistrering>
    fun find(params: VektregistreringFindParams): Either<RepositoryError, List<Vektregistrering>>
}