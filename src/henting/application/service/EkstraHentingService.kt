package ombruk.backend.henting.application.service

import arrow.core.Either
import io.ktor.locations.*
import ombruk.backend.henting.application.api.dto.EkstraHentingDeleteDto
import ombruk.backend.henting.application.api.dto.EkstraHentingFindDto
import ombruk.backend.henting.application.api.dto.EkstraHentingSaveDto
import ombruk.backend.henting.application.api.dto.EkstraHentingUpdateDto
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.port.IEkstraHentingRepository
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.utlysning.application.service.UtlysningService
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

@KtorExperimentalLocationsAPI
class EkstraHentingService(val ekstraHentingRepository: IEkstraHentingRepository, val utlysningService: UtlysningService): IEkstraHentingService {
    override fun create(dto: EkstraHentingSaveDto): Either<ServiceError, EkstraHenting> {
        return transaction { ekstraHentingRepository.insert(dto) }
    }

    override fun findOne(id: UUID): Either<ServiceError, EkstraHenting> {
        return transaction { ekstraHentingRepository.findOne(id) }
    }

    override fun find(dto: EkstraHentingFindDto): Either<ServiceError, List<EkstraHenting>> {
        return transaction { ekstraHentingRepository.find(dto) }
    }

    override fun delete(dto: EkstraHentingDeleteDto): Either<ServiceError, Unit> {
        return transaction { ekstraHentingRepository.delete(dto.id) }
    }

    override fun update(dto: EkstraHentingUpdateDto): Either<ServiceError, EkstraHenting> {
        return transaction { ekstraHentingRepository.update(dto) }
    }

}