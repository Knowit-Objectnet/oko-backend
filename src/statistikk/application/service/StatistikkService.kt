package ombruk.backend.statistikk.application.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.aktor.infrastructure.table.PartnerTable
import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.avtale.infrastructure.table.AvtaleTable
import ombruk.backend.henting.application.api.dto.HentingFindDto
import ombruk.backend.henting.application.service.IHentingService
import ombruk.backend.henting.infrastructure.table.EkstraHentingTable
import ombruk.backend.henting.infrastructure.table.HenteplanTable
import ombruk.backend.henting.infrastructure.table.PlanlagtHentingTable
import ombruk.backend.kategori.application.api.dto.KategoriFindDto
import ombruk.backend.kategori.application.service.IKategoriService
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.kategori.infrastructure.table.KategoriTable
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.statistikk.application.api.dto.StatistikkFindDto
import ombruk.backend.statistikk.domain.entity.KategoriStatistikk
import ombruk.backend.statistikk.domain.entity.StasjonStatistikk
import ombruk.backend.statistikk.domain.entity.Statistikk
import ombruk.backend.vektregistrering.infrastructure.table.VektregistreringTable
import org.h2.table.Plan
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class StatistikkService(val hentingService: IHentingService, val kategoriService: IKategoriService) : IStatistikkService {

    fun queryStatistikkAggregert(dto: StatistikkFindDto): Query {
        val stasjonAlias = StasjonTable.alias("stasjonAktorAlias")
        val planlagtHenting = PlanlagtHentingTable
            .innerJoin(HenteplanTable, { henteplanId }, { id })
            .innerJoin(AvtaleTable, { HenteplanTable.avtaleId }, { id })
            .innerJoin(StasjonTable, { HenteplanTable.stasjonId }, { id })
            .leftJoin(PartnerTable, { AvtaleTable.aktorId }, { id })
            .leftJoin(stasjonAlias, { AvtaleTable.aktorId }, { stasjonAlias[StasjonTable.id] })
            .innerJoin(VektregistreringTable, { PlanlagtHentingTable.id }, { hentingId })
            .innerJoin(KategoriTable, { VektregistreringTable.kategoriId }, { id })

        val query = planlagtHenting.slice(PartnerTable.navn, StasjonTable.navn, KategoriTable.navn, KategoriTable.id, VektregistreringTable.vekt.sum()).selectAll()

        dto.partnerId?.let{ query.andWhere { PartnerTable.id eq it } }
        dto.stasjonId?.let { query.andWhere { StasjonTable.id eq it } }
        dto.kategoriId?.let { query.andWhere { VektregistreringTable.kategoriId eq it } }
        dto.after?.let { query.andWhere { PlanlagtHentingTable.startTidspunkt.greaterEq(it) } }
        dto.before?.let { query.andWhere { PlanlagtHentingTable.sluttTidspunkt.lessEq(it) } }

        query.groupBy(PartnerTable.navn, StasjonTable.navn, KategoriTable.navn, KategoriTable.id)

        return query
    }

    override fun find(dto: StatistikkFindDto): Either<ServiceError, List<Statistikk>> = transaction {
        val query = queryStatistikkAggregert(dto)

        fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
            return map {
                if (block(it)) newValue else it
            }
        }

        val statistikkMap = mutableMapOf<String, Statistikk>()

        query.mapNotNull {
            val row = it
            var statistikk = statistikkMap.get(row[PartnerTable.navn]) ?: Statistikk(partnerNavn = row[PartnerTable.navn])
            var stasjon = statistikk.stasjoner.find { it.stasjonNavn == row[StasjonTable.navn] } ?: StasjonStatistikk(stasjonNavn = row[StasjonTable.navn])
            stasjon = stasjon.copy(kategorier = stasjon.kategorier + KategoriStatistikk(kategoriId = row[KategoriTable.id].value, kategoriNavn = row[KategoriTable.navn], vekt = row[VektregistreringTable.vekt.sum()] ?: 0f))

            if (statistikk.stasjoner.find { it.stasjonNavn == stasjon.stasjonNavn } != null) {
                val newStasjoner =
                    statistikk.stasjoner.replace(stasjon) { it.stasjonNavn == stasjon.stasjonNavn }
                statistikk = statistikk.copy(stasjoner = newStasjoner)
            } else statistikk = statistikk.copy(stasjoner = statistikk.stasjoner + stasjon)

            statistikkMap.put(statistikk.partnerNavn, statistikk)
        }
        statistikkMap.values.toList().right()
    }
}