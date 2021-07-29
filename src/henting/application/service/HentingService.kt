package ombruk.backend.henting.application.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.align.empty
import arrow.core.extensions.list.traverse.sequence
import io.ktor.locations.*
import ombruk.backend.henting.application.api.dto.EkstraHentingFindDto
import ombruk.backend.henting.application.api.dto.HentingFindDto
import ombruk.backend.henting.application.api.dto.PlanlagtHentingFindDto
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.entity.HentingWrapper
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.model.HentingType
import ombruk.backend.kategori.application.api.dto.EkstraHentingKategoriFindDto
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringFindDto
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.collections.ArrayList

@KtorExperimentalLocationsAPI
class HentingService(val planlagtHentingService: IPlanlagtHentingService, val ekstraHentingService: IEkstraHentingService): IHentingService {
    override fun findOne(id: UUID, aktorId: UUID?): Either<ServiceError, HentingWrapper> {
        return transaction {
            planlagtHentingService.findOne(id)
                .fold(
                    {
                        ekstraHentingService.findOneWithUtlysninger(id, aktorId).fold(
                            { ServiceError("Not found").left() },
                            { wrapperFromEkstra(it).right() }
                        )
                    },
                    { wrapperFromPlanlagt(it).right() }
                )
        }
    }

    override fun find(dto: HentingFindDto, aktorId: UUID?): Either<ServiceError, List<HentingWrapper>> {
        return transaction {
            val hentinger: MutableList<HentingWrapper> = mutableListOf()
            ekstraHentingService.findWithUtlysninger(
                EkstraHentingFindDto(
                    id = dto.id,
                    stasjonId = dto.stasjonId,
                    aktorId = dto.aktorId,
                    before = dto.before,
                    after = dto.after
                ), aktorId
            ).fold( {it.left()},
                { it.map { hentinger.add(wrapperFromEkstra(it)) }
                    planlagtHentingService.find(PlanlagtHentingFindDto(
                        id = dto.id,
                        before = dto.before,
                        after = dto.after,
                        stasjonId = dto.stasjonId,
                        aktorId = dto.aktorId))
                        .fold({it.left()}, {
                            it.map {hentinger.add(wrapperFromPlanlagt(it))}
                            hentinger.right()
                        })
                })
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
        HentingType.EKSTRA,
        null,
        henting,
        henting.stasjonId,
        henting.stasjonNavn,
        henting.godkjentUtlysning?.partnerId,
        henting.godkjentUtlysning?.partnerNavn
    )
}
