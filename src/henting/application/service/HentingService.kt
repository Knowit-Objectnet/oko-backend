package ombruk.backend.henting.application.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.*
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.entity.HentingWrapper
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.model.HentingType
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

@KtorExperimentalLocationsAPI
class HentingService(val planlagtHentingService: IPlanlagtHentingService, val ekstraHentingService: IEkstraHentingService): IHentingService {
    override fun findOne(id: UUID): Either<ServiceError, HentingWrapper> {
        return transaction {
            planlagtHentingService.findOne(id)
                .fold(
                    {
                        ekstraHentingService.findOne(id).fold(
                            { ServiceError("Not found").left() },
                            { wrapperFromEkstra(it).right() }
                        )
                    },
                    { wrapperFromPlanlagt(it).right() }
                )
        }
    }
}

fun wrapperFromPlanlagt(henting: PlanlagtHenting): HentingWrapper {
    return HentingWrapper(
        henting.id,
        henting.startTidspunkt,
        henting.sluttTidspunkt,
        HentingType.PLANLAGT,
        henting,
        null,
        henting.stasjonId,
        henting.stasjonNavn,
        henting.aktorId,
        henting.aktorNavn
    )
}
fun wrapperFromEkstra(henting: EkstraHenting): HentingWrapper {
    return HentingWrapper(
        henting.id,
        henting.startTidspunkt,
        henting.sluttTidspunkt,
        HentingType.PLANLAGT,
        null,
        henting,
        henting.stasjonId,
        henting.stasjonNavn,
        henting.godkjentUtlysning?.partnerId,
        henting.godkjentUtlysning?.partnerNavn
    )
}
