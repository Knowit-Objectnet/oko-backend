package ombruk.backend.henting.application.service

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import io.ktor.locations.*
import ombruk.backend.henting.application.api.dto.*
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.entity.PlanlagtHentingWithParents
import ombruk.backend.henting.domain.port.IEkstraHentingRepository
import ombruk.backend.henting.domain.port.IPlanlagtHentingRepository
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
class EkstraHentingService(val planlagtHentingRepository: IEkstraHentingRepository): IEkstraHentingService {
    override fun create(dto: EkstraHentingSaveDto): Either<ServiceError, EkstraHenting> {
        return transaction { planlagtHentingRepository.insert(dto) }
    }

    override fun findOne(id: UUID): Either<ServiceError, EkstraHenting> {
        return transaction { planlagtHentingRepository.findOne(id) }
    }

    override fun find(dto: EkstraHentingFindDto): Either<ServiceError, List<EkstraHenting>> {
        return transaction { planlagtHentingRepository.find(dto) }
    }

    override fun delete(dto: EkstraHentingDeleteDto): Either<ServiceError, Unit> {
        return transaction { planlagtHentingRepository.delete(dto.id) }
    }

    override fun update(dto: EkstraHentingUpdateDto): Either<ServiceError, EkstraHenting> {
        return transaction { planlagtHentingRepository.update(dto) }
    }

}