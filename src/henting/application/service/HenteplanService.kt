package ombruk.backend.henting.application.service

import arrow.core.Either
import henting.application.api.dto.HenteplanPostDto
import ombruk.backend.henting.application.api.dto.HenteplanDeleteDto
import ombruk.backend.henting.application.api.dto.HenteplanFindDto
import ombruk.backend.henting.application.api.dto.HenteplanUpdateDto
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.port.IHenteplanRepository
import ombruk.backend.shared.error.ServiceError

class HenteplanService(henteplanRepository: IHenteplanRepository) : IHenteplanService {
    override fun create(dto: HenteplanPostDto): Either<ServiceError, Henteplan> {
        TODO("Not yet implemented")
    }

    override fun batchCreate(dto: List<HenteplanPostDto>): Either<ServiceError, List<Henteplan>> {
        TODO("Not yet implemented")
    }

    override fun findOne(id: Int): Either<ServiceError, Henteplan> {
        TODO("Not yet implemented")
    }

    override fun find(dto: HenteplanFindDto): Either<ServiceError, List<Henteplan>> {
        TODO("Not yet implemented")
    }

    override fun delete(dto: HenteplanDeleteDto): Either<ServiceError, Unit> {
        TODO("Not yet implemented")
    }

    override fun update(dto: HenteplanUpdateDto): Either<ServiceError, Henteplan> {
        TODO("Not yet implemented")
    }
}