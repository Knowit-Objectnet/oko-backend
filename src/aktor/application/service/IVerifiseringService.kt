package ombruk.backend.aktor.application.service

import ombruk.backend.shared.error.ServiceError


import arrow.core.Either
import io.ktor.locations.*
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.domain.entity.Verifisering
import ombruk.backend.aktor.domain.entity.VerifiseringStatus
import java.util.*

interface IVerifiseringService {

    fun save(dto: VerifiseringSaveDto): Either<ServiceError, Verifisering>

    fun getVerifiseringById(id: UUID): Either<ServiceError, Verifisering>

    @KtorExperimentalLocationsAPI
    fun verifiser(dto: KontaktVerifiseringDto): Either<ServiceError, VerifiseringStatus>

    fun deleteVerifiseringById(id: UUID): Either<ServiceError, Verifisering>

    fun update(dto: VerifiseringUpdateDto): Either<ServiceError, Verifisering>
    fun getVerifisertById(id: UUID): Either<ServiceError, VerifiseringStatus>
}
