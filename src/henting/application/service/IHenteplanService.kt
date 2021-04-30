package ombruk.backend.henting.application.service

import arrow.core.Either
import henting.application.api.dto.HenteplanPostDto
import ombruk.backend.henting.application.api.dto.HenteplanDeleteDto
import ombruk.backend.henting.application.api.dto.HenteplanFindDto
import ombruk.backend.henting.application.api.dto.HenteplanUpdateDto
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.shared.error.ServiceError

interface IHenteplanService {
    fun create(dto: HenteplanPostDto): Either<ServiceError, Henteplan>

    fun batchCreate(dto: List<HenteplanPostDto>): Either<ServiceError, List<Henteplan>>

    fun findOne(id: Int): Either<ServiceError, Henteplan>

    fun find(dto: HenteplanFindDto): Either<ServiceError, List<Henteplan>>

    fun delete(dto: HenteplanDeleteDto): Either<ServiceError, Unit>

    fun update(dto: HenteplanUpdateDto): Either<ServiceError, Henteplan>
}