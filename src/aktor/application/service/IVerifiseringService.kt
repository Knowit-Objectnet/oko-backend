package ombruk.backend.aktor.application.service

import ombruk.backend.shared.error.ServiceError


import arrow.core.Either
import io.ktor.locations.*
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.domain.entity.Verifisering
import ombruk.backend.aktor.domain.entity.Verifisert
import ombruk.backend.aktor.domain.model.VerifiseringUpdateParams
import java.util.*

interface IVerifiseringService {

    fun save(dto: VerifiseringSaveDto): Either<ServiceError, Verifisering>

    fun getVerifiseringById(id: UUID): Either<ServiceError, Verifisering>

    @KtorExperimentalLocationsAPI
    fun verifiser(dto: KontaktVerifiseringDto): Either<ServiceError, Verifisert>

    fun deleteVerifiseringById(id: UUID): Either<ServiceError, Verifisering>

    fun update(dto: VerifiseringUpdateDto): Either<ServiceError, Verifisering>
}
