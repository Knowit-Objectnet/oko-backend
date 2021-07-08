package ombruk.backend.aarsak.application.service

import arrow.core.Either
import ombruk.backend.aarsak.application.api.dto.AarsakFindDto
import ombruk.backend.aarsak.application.api.dto.AarsakSaveDto
import ombruk.backend.aarsak.application.api.dto.AarsakUpdateDto
import ombruk.backend.aarsak.domain.entity.Aarsak
import ombruk.backend.aktor.application.api.dto.KontaktSaveDto
import ombruk.backend.aktor.application.api.dto.KontaktUpdateDto
import ombruk.backend.aktor.domain.entity.Aktor
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.enum.AktorType
import ombruk.backend.shared.error.ServiceError
import java.util.*

interface IAarsakService {
    fun findOne(id: UUID): Either<ServiceError, Aarsak>
    fun find(dto: AarsakFindDto): Either<ServiceError, List<Aarsak>>
    fun save(dto: AarsakSaveDto): Either<ServiceError, Aarsak>
    fun delete(id: UUID): Either<ServiceError, Aarsak>
    fun update(dto: AarsakUpdateDto): Either<ServiceError, Aarsak>
    fun archiveOne(id: UUID): Either<ServiceError, Unit>
}