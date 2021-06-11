package ombruk.backend.avtale.application.service

import arrow.core.Either
import avtale.application.api.dto.AvtaleSaveDto
import ombruk.backend.avtale.application.api.dto.AvtaleDeleteDto
import ombruk.backend.avtale.application.api.dto.AvtaleFindDto
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.shared.error.ServiceError
import java.util.*

interface IAvtaleService {
    fun save(dto: AvtaleSaveDto): Either<ServiceError, Avtale>

    fun findOne(id: UUID): Either<ServiceError, Avtale>

    fun find(dto: AvtaleFindDto): Either<ServiceError, List<Avtale>>

    fun delete(dto: AvtaleDeleteDto): Either<ServiceError, Unit>

    fun archiveOne(id: UUID): Either<ServiceError, Unit>
}