package ombruk.backend.utlysning.application.service

import arrow.core.Either
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.utlysning.application.api.dto.UtlysningDeleteDto
import ombruk.backend.utlysning.application.api.dto.UtlysningFindDto
import ombruk.backend.utlysning.application.api.dto.UtlysningSaveDto
import ombruk.backend.utlysning.domain.entity.Utlysning
import java.util.*

interface IUtlysningService {
    fun save(dto: UtlysningSaveDto): Either<ServiceError, Utlysning>

    fun findOne(id: UUID): Either<ServiceError, Utlysning>

    fun find(dto: UtlysningFindDto): Either<ServiceError, List<Utlysning>>

    fun delete(dto: UtlysningDeleteDto): Either<ServiceError, Unit>
}