package ombruk.backend.vektregistrering.application.service

import arrow.core.Either
import ombruk.backend.kategori.application.api.dto.KategoriDeleteDto
import ombruk.backend.kategori.application.api.dto.KategoriFindDto
import ombruk.backend.kategori.application.api.dto.KategoriSaveDto
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.utlysning.application.api.dto.UtlysningBatchSaveDto
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.vektregistrering.application.api.dto.*
import ombruk.backend.vektregistrering.domain.entity.Vektregistrering
import java.util.*

interface IVektregistreringService {
    fun save(dto: VektregistreringSaveDto): Either<ServiceError, Vektregistrering>
    fun batchSave(dto: VektregistreringBatchSaveDto): Either<ServiceError, List<Vektregistrering>>
    fun findOne(id: UUID): Either<ServiceError, Vektregistrering>
    fun find(dto: VektregistreringFindDto): Either<ServiceError, List<Vektregistrering>>
    fun delete(id: UUID): Either<ServiceError, Unit>
    fun update(dto: VektregistreringUpdateDto): Either<ServiceError, Vektregistrering>
    fun batchUpdate(dto: VektregistreringBatchUpdateDto): Either<ServiceError, List<Vektregistrering>>
}