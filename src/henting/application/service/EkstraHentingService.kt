package ombruk.backend.henting.application.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.*
import ombruk.backend.henting.application.api.dto.EkstraHentingDeleteDto
import ombruk.backend.henting.application.api.dto.EkstraHentingFindDto
import ombruk.backend.henting.application.api.dto.EkstraHentingSaveDto
import ombruk.backend.henting.application.api.dto.EkstraHentingUpdateDto
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.params.EkstraHentingFindParams
import ombruk.backend.henting.domain.port.IEkstraHentingRepository
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.utlysning.application.service.UtlysningService
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

@KtorExperimentalLocationsAPI
class EkstraHentingService(val ekstraHentingRepository: IEkstraHentingRepository): IEkstraHentingService {
    override fun save(dto: EkstraHentingSaveDto): Either<ServiceError, EkstraHenting> {
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

    override fun archive(params: EkstraHentingFindParams): Either<ServiceError, Unit> {
        return transaction {
            ekstraHentingRepository.archive(params)
                .fold(
                    {Either.Left(ServiceError(it.message))},
                    {Either.Right(Unit)}
                )
                .fold({rollback(); it.left()}, {it.right()})
        }
    }

    override fun archiveOne(id: UUID): Either<ServiceError, Unit> {
        return transaction {
            ekstraHentingRepository.archiveOne(id)
                .fold(
                    {Either.Left(ServiceError(it.message))},
                    {Either.Right(Unit)}
                )
                .fold({rollback(); it.left()}, {it.right()})
        }
    }
}