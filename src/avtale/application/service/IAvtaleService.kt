package ombruk.backend.avtale.application.service

import arrow.core.Either
import avtale.application.api.dto.AvtalePostDto
import ombruk.backend.avtale.application.api.dto.AvtaleDeleteDto
import ombruk.backend.avtale.application.api.dto.AvtaleFindDto
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.shared.error.ServiceError

interface IAvtaleService {
    fun save(dto: AvtalePostDto): Either<ServiceError, Avtale>

    fun findOne(id: Int): Either<ServiceError, Avtale>

    fun find(dto: AvtaleFindDto): Either<ServiceError, List<Avtale>>

    fun delete(dto: AvtaleDeleteDto): Either<ServiceError, Unit>
}