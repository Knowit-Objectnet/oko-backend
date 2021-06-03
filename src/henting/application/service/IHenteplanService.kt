package ombruk.backend.henting.application.service

import arrow.core.Either
import henting.application.api.dto.HenteplanSaveDto
import ombruk.backend.henting.application.api.dto.HenteplanDeleteDto
import ombruk.backend.henting.application.api.dto.HenteplanFindDto
import ombruk.backend.henting.application.api.dto.HenteplanUpdateDto
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.shared.error.ServiceError
import java.util.*

interface IHenteplanService {
    fun save(dto: HenteplanSaveDto): Either<ServiceError, Henteplan>

    fun batchSave(dto: List<HenteplanSaveDto>): Either<ServiceError, List<Henteplan>>

    fun findOne(id: UUID): Either<ServiceError, Henteplan>

    fun find(dto: HenteplanFindDto): Either<ServiceError, List<Henteplan>>

    fun findAllForAvtale(avtaleId: UUID): Either<ServiceError, List<Henteplan>>

    fun delete(dto: HenteplanDeleteDto): Either<ServiceError, Unit>

    fun update(dto: HenteplanUpdateDto): Either<ServiceError, Henteplan>
}