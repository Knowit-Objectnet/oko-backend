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

    //TODO: Lag en løsning for å søke etter planalgt henting ut i fra aktør og stasjon? kanskje bruke Henteplaner som ikke er utgått?
    override fun find(dto: HentingFindDto): Either<ServiceError, List<HentingWrapper>> {
        return transaction {
            val hentinger: MutableList<HentingWrapper> = mutableListOf()
            ekstraHentingService.find(
                EkstraHentingFindDto(
                    id = dto.id,
                    stasjonId = dto.stasjonId,
                    aktorId = dto.aktorId,
                    before = dto.before,
                    after = dto.after
                )
            ).map { it.map { hentinger.add(wrapperFromEkstra(it)) } }
            planlagtHentingService.find(PlanlagtHentingFindDto(id = dto.id, before = dto.before, after = dto.after))
                .map {
                    if (dto.aktorId != null) it.filter { it.aktorId == dto.aktorId }
                    if (dto.stasjonId != null) it.filter { it.stasjonId == dto.stasjonId }
                    it.map { hentinger.add(wrapperFromPlanlagt(it)) }
                }
            if (hentinger.isEmpty()) ServiceError("Ingen hentinger med de parametrene").left()
            else hentinger.right()
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
