package ombruk.backend.henting.application.service

import arrow.core.Either
import ombruk.backend.henting.application.api.dto.*
import ombruk.backend.henting.domain.entity.Henting
import ombruk.backend.shared.error.ServiceError
import java.util.*

interface IHentingService {
    fun findOne(id: UUID): Either<ServiceError, Henting>
}