package ombruk.backend.aarsak.application.service

import arrow.core.Either
import ombruk.backend.aarsak.application.api.dto.AarsakFindDto
import ombruk.backend.aarsak.application.api.dto.AarsakSaveDto
import ombruk.backend.aarsak.application.api.dto.AarsakUpdateDto
import ombruk.backend.aarsak.domain.entity.Aarsak
import ombruk.backend.shared.error.ServiceError
import java.util.*

class AarsakService(

) : IAarsakService {
    override fun findOne(id: UUID): Either<ServiceError, Aarsak> {
        TODO("Not yet implemented")
    }

    override fun find(dto: AarsakFindDto): Either<ServiceError, List<Aarsak>> {
        TODO("Not yet implemented")
    }

    override fun save(dto: AarsakSaveDto): Either<ServiceError, Aarsak> {
        TODO("Not yet implemented")
    }

    override fun delete(id: UUID): Either<ServiceError, Aarsak> {
        TODO("Not yet implemented")
    }

    override fun update(dto: AarsakUpdateDto): Either<ServiceError, Aarsak> {
        TODO("Not yet implemented")
    }


}