package ombruk.backend.kategori.application.service

import arrow.core.Either
import ombruk.backend.kategori.application.api.dto.*
import ombruk.backend.kategori.domain.entity.HenteplanKategori
import ombruk.backend.kategori.domain.params.HenteplanKategoriFindParams
import ombruk.backend.shared.error.ServiceError
import java.util.*

interface IEkstraHentingKategoriService {
    fun save(dto: HenteplanKategoriSaveDto): Either<ServiceError, HenteplanKategori>

    fun findOne(id: UUID): Either<ServiceError, HenteplanKategori>

    fun find(dto: HenteplanKategoriFindDto): Either<ServiceError, List<HenteplanKategori>>

    fun delete(dto: HenteplanKategoriDeleteDto): Either<ServiceError, Unit>

    fun archive(params: HenteplanKategoriFindParams): Either<ServiceError, Unit>

    fun archiveOne(id: UUID): Either<ServiceError, Unit>
}