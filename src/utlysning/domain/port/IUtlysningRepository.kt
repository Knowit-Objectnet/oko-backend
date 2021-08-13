package ombruk.backend.utlysning.domain.port

import arrow.core.Either
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.domain.params.*
import java.util.*

interface IUtlysningRepository {
    fun insert(params: UtlysningCreateParams): Either<RepositoryError, Utlysning>
    fun update(params: UtlysningUpdateParams): Either<RepositoryError, Utlysning>
    fun delete(id: UUID): Either<RepositoryError, Unit>
    fun findOne(id: UUID): Either<RepositoryError, Utlysning>
    fun find(params: UtlysningFindParams): Either<RepositoryError, List<Utlysning>>
    fun acceptPartner(params: UtlysningPartnerAcceptParams): Either<RepositoryError, Utlysning>
    fun acceptStasjon(params: UtlysningStasjonAcceptParams): Either<RepositoryError, Utlysning>
    fun archive(params: UtlysningFindParams): Either<RepositoryError, List<Utlysning>>
    fun archiveOne(id: UUID): Either<RepositoryError,Utlysning>
}