package ombruk.backend.utlysning.application.service

import arrow.core.Either
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.utlysning.application.api.dto.*
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.domain.params.UtlysningFindParams
import java.util.*

interface IUtlysningService {
    fun save(dto: UtlysningSaveDto): Either<ServiceError, Utlysning>

    fun findOne(id: UUID): Either<ServiceError, Utlysning>

    fun find(dto: UtlysningFindDto): Either<ServiceError, List<Utlysning>>

    fun delete(dto: UtlysningDeleteDto): Either<ServiceError, Unit>

    fun batchSave(dto: UtlysningBatchSaveDto): Either<ServiceError, List<Utlysning>>

    fun partnerAccept(dtoPartner: UtlysningPartnerAcceptDto): Either<ServiceError, Utlysning>

    fun stasjonAccept(dtoPartner: UtlysningStasjonAcceptDto): Either<ServiceError, Utlysning>

    fun findAccepted(ekstraHentingId: UUID): Either<ServiceError, Utlysning?>

    fun archive(params: UtlysningFindParams): Either<ServiceError, Unit>

    fun archiveOne(id: UUID): Either<ServiceError, Unit>
}