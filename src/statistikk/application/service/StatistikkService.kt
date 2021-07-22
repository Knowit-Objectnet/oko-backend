package ombruk.backend.statistikk.application.service

import arrow.core.Either
import arrow.core.right
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.statistikk.application.api.dto.StatistikkFindDto
import ombruk.backend.statistikk.domain.entity.Statistikk

class StatistikkService() : IStatistikkService {
    override fun find(dto: StatistikkFindDto): Either<ServiceError, List<Statistikk>> {
        val s = Statistikk()
        val sL = listOf<Statistikk>(s)
        return sL.right()
    }
}