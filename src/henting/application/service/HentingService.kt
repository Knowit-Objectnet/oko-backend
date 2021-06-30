package ombruk.backend.henting.application.service

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import io.ktor.locations.*
import ombruk.backend.henting.application.api.dto.*
import ombruk.backend.henting.domain.entity.Henting
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.entity.PlanlagtHentingWithParents
import ombruk.backend.henting.domain.params.PlanlagtHentingFindParams
import ombruk.backend.henting.domain.port.IPlanlagtHentingRepository
import ombruk.backend.kategori.application.api.dto.HenteplanKategoriFindDto
import ombruk.backend.kategori.application.service.IHenteplanKategoriService
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

@KtorExperimentalLocationsAPI
class HentingService(val planlagtHentingService: IPlanlagtHentingService, val ekstraHentingService: IEkstraHentingService): IHentingService {
    override fun findOne(id: UUID): Either<ServiceError, Henting> {
        return transaction {
            planlagtHentingService.findOne(id)
                .fold(
                    {
                        ekstraHentingService.findOne(id).fold(
                            { ServiceError("Not found").left() },
                            { it.right() }
                        )
                    },
                    { it.right() }
                )
        }
    }
}