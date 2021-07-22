package ombruk.backend.statistikk.application.service

import arrow.core.Either
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.statistikk.application.api.dto.StatistikkFindDto
import ombruk.backend.statistikk.domain.entity.Statistikk

interface IStatistikkService {
    fun find(dto: StatistikkFindDto): Either<ServiceError, List<Statistikk>>
}