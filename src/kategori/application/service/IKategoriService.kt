package ombruk.backend.kategori.application.service

import arrow.core.Either
import ombruk.backend.kategori.application.api.dto.KategoriDeleteDto
import ombruk.backend.kategori.application.api.dto.KategoriFindDto
import ombruk.backend.kategori.application.api.dto.KategoriSaveDto
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.shared.error.ServiceError
import java.util.*

interface IKategoriService {
    fun save(dto: KategoriSaveDto): Either<ServiceError, Kategori>

    fun findOne(id: UUID): Either<ServiceError, Kategori>

    fun find(dto: KategoriFindDto): Either<ServiceError, List<Kategori>>

    fun delete(dto: KategoriDeleteDto): Either<ServiceError, Unit>

    fun archiveOne(id: UUID): Either<ServiceError, Unit>
}