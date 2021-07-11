package ombruk.backend.aktor.domain.port

import arrow.core.Either
import ombruk.backend.aktor.domain.entity.Verifisering
import ombruk.backend.aktor.domain.model.*
import ombruk.backend.shared.error.RepositoryError
import java.util.*

interface IVerifiseringRepository {
    fun findOne(id: UUID): Either<RepositoryError, Verifisering>
    fun insert(params: VerifiseringCreateParams): Either<RepositoryError, Verifisering>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun update(params: VerifiseringUpdateParams): Either<RepositoryError, Verifisering>
}