package ombruk.backend.henting.application.service

import arrow.core.Either
import ombruk.backend.henting.application.api.dto.*
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.params.EkstraHentingFindParams
import ombruk.backend.shared.error.ServiceError
import java.util.*

interface IEkstraHentingService {

    fun save(dto: EkstraHentingSaveDto): Either<ServiceError, EkstraHenting>

    fun findOne(id: UUID): Either<ServiceError, EkstraHenting>

    fun find(dto: EkstraHentingFindDto): Either<ServiceError, List<EkstraHenting>>

    fun delete(dto: EkstraHentingDeleteDto): Either<ServiceError, Unit>

    fun update(dto: EkstraHentingUpdateDto): Either<ServiceError, EkstraHenting>

    fun archive(params: EkstraHentingFindParams): Either<ServiceError, Unit>

    fun archiveOne(id: UUID): Either<ServiceError, Unit>
    fun findWithUtlysninger(dto: EkstraHentingFindDto): Either<ServiceError, List<EkstraHenting>>
}