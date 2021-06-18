package ombruk.backend.kategori.application.service

import arrow.core.Either
import ombruk.backend.kategori.application.api.dto.*
import ombruk.backend.kategori.domain.entity.EkstraHentingKategori
import ombruk.backend.kategori.domain.params.EkstraHentingKategoriFindParams
import ombruk.backend.shared.error.ServiceError
import java.util.*

interface IEkstraHentingKategoriService {
    fun save(dto: EkstraHentingKategoriSaveDto): Either<ServiceError, EkstraHentingKategori>

    fun findOne(id: UUID): Either<ServiceError, EkstraHentingKategori>

    fun find(dto: EkstraHentingKategoriFindDto): Either<ServiceError, List<EkstraHentingKategori>>

    fun delete(dto: EkstraHentingKategoriDeleteDto): Either<ServiceError, Unit>

    fun archive(params: EkstraHentingKategoriFindParams): Either<ServiceError, Unit>

    fun archiveOne(id: UUID): Either<ServiceError, Unit>
}