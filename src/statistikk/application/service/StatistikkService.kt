package ombruk.backend.statistikk.application.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.aktor.domain.entity.Partner
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
import ombruk.backend.utlysning.infrastructure.table.UtlysningTable
import ombruk.backend.vektregistrering.infrastructure.table.VektregistreringTable
import org.h2.table.Plan
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class StatistikkService(val hentingService: IHentingService, val kategoriService: IKategoriService) : IStatistikkService {

    fun queryStatistikkAggregert(dto: StatistikkFindDto): Pair<Query, Query> {
        val stasjonAlias = StasjonTable.alias("stasjonAktorAlias")
        val planlagtHenting = PlanlagtHentingTable
            .innerJoin(HenteplanTable, { henteplanId }, { id })
            .innerJoin(AvtaleTable, { HenteplanTable.avtaleId }, { id })
            .innerJoin(StasjonTable, { HenteplanTable.stasjonId }, { id })
            .leftJoin(PartnerTable, { AvtaleTable.aktorId }, { id })
            .leftJoin(stasjonAlias, { AvtaleTable.aktorId }, { stasjonAlias[StasjonTable.id] })
            .innerJoin(VektregistreringTable, { PlanlagtHentingTable.id }, { hentingId })
            .innerJoin(KategoriTable, { VektregistreringTable.kategoriId }, { id })

        val queryPlanlagtHenting = planlagtHenting.slice(PartnerTable.navn, StasjonTable.navn, KategoriTable.navn, KategoriTable.id, VektregistreringTable.vekt.sum()).select(where = {PlanlagtHentingTable.avlyst.isNull()})

        val ekstraHenting = EkstraHentingTable
            .innerJoin(UtlysningTable, { EkstraHentingTable.id }, { hentingId })
            .innerJoin(StasjonTable, { EkstraHentingTable.stasjonId }, { id })
            .leftJoin(PartnerTable, { UtlysningTable.partnerId }, { id })
            .leftJoin(stasjonAlias, { UtlysningTable.partnerId }, { stasjonAlias[StasjonTable.id] })
            .innerJoin(VektregistreringTable, { EkstraHentingTable.id }, { hentingId })
            .innerJoin(KategoriTable, { VektregistreringTable.kategoriId }, { id })

        val queryEkstraHenting = ekstraHenting.slice(PartnerTable.navn, StasjonTable.navn, KategoriTable.navn, KategoriTable.id, VektregistreringTable.vekt.sum()).select(where = {UtlysningTable.partnerPameldt.isNotNull()})

        dto.partnerId?.let{ queryPlanlagtHenting.andWhere { PartnerTable.id eq it }; queryEkstraHenting.andWhere { PartnerTable.id eq it } }
        dto.stasjonId?.let { queryPlanlagtHenting.andWhere { StasjonTable.id eq it }; queryEkstraHenting.andWhere { StasjonTable.id eq it } }
        dto.kategoriId?.let { queryPlanlagtHenting.andWhere { VektregistreringTable.kategoriId eq it }; queryEkstraHenting.andWhere { VektregistreringTable.kategoriId eq it }  }
        dto.after?.let { queryPlanlagtHenting.andWhere { PlanlagtHentingTable.startTidspunkt.greaterEq(it) }; queryEkstraHenting.andWhere { EkstraHentingTable.startTidspunkt.greaterEq(it) } }
        dto.before?.let { queryPlanlagtHenting.andWhere { PlanlagtHentingTable.sluttTidspunkt.lessEq(it) }; queryEkstraHenting.andWhere { EkstraHentingTable.sluttTidspunkt.lessEq(it) } }

        queryPlanlagtHenting.groupBy(PartnerTable.navn, StasjonTable.navn, KategoriTable.navn, KategoriTable.id)
        queryEkstraHenting.groupBy(PartnerTable.navn, StasjonTable.navn, KategoriTable.navn, KategoriTable.id)

        return Pair(queryPlanlagtHenting, queryEkstraHenting)
    }

    override fun find(dto: StatistikkFindDto): Either<ServiceError, List<Statistikk>> = transaction {
        val queries = queryStatistikkAggregert(dto)

        fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
            return map {
                if (block(it)) newValue else it
            }
        }

        val statistikkMap = mutableMapOf<String, Statistikk>()

        queries.first.mapNotNull {
            val row = it
            var statistikk = statistikkMap.get(row[PartnerTable.navn]) ?: Statistikk(partnerNavn = row[PartnerTable.navn])
            var stasjon = statistikk.stasjoner.find { it.stasjonNavn == row[StasjonTable.navn] } ?: StasjonStatistikk(stasjonNavn = row[StasjonTable.navn])
            stasjon = stasjon.copy(kategorier = stasjon.kategorier + KategoriStatistikk(kategoriId = row[KategoriTable.id].value, kategoriNavn = row[KategoriTable.navn], vekt = row[VektregistreringTable.vekt.sum()] ?: 0f))

            if (statistikk.stasjoner.find { it.stasjonNavn == stasjon.stasjonNavn } != null) statistikk = statistikk.copy(stasjoner = statistikk.stasjoner.replace(stasjon) { it.stasjonNavn == stasjon.stasjonNavn })
            else statistikk = statistikk.copy(stasjoner = statistikk.stasjoner + stasjon)

            statistikkMap.put(statistikk.partnerNavn, statistikk)
        }

        queries.second.mapNotNull {
            val row = it
            var statistikk = statistikkMap.get(row[PartnerTable.navn]) ?: Statistikk(partnerNavn = row[PartnerTable.navn])
            var stasjon = statistikk.stasjoner.find { it.stasjonNavn == row[StasjonTable.navn] } ?: StasjonStatistikk(stasjonNavn = row[StasjonTable.navn])
            var kategoriStatistikk = stasjon.kategorier.find { it.kategoriId == row[KategoriTable.id].value } ?: KategoriStatistikk(kategoriId = row[KategoriTable.id].value, kategoriNavn = row[KategoriTable.navn], vekt = row[VektregistreringTable.vekt.sum()] ?: 0f)
            if (stasjon.kategorier.find { it.kategoriId == row[KategoriTable.id].value } != null) stasjon = stasjon.copy(kategorier = stasjon.kategorier.replace(kategoriStatistikk.copy(vekt = kategoriStatistikk.vekt + (row[VektregistreringTable.vekt.sum()] ?: 0f))) { it.kategoriId == kategoriStatistikk.kategoriId })
            else stasjon = stasjon.copy(kategorier = stasjon.kategorier + KategoriStatistikk(kategoriId = row[KategoriTable.id].value, kategoriNavn = row[KategoriTable.navn], vekt = row[VektregistreringTable.vekt.sum()] ?: 0f))

            if (statistikk.stasjoner.find { it.stasjonNavn == stasjon.stasjonNavn } != null) statistikk = statistikk.copy(stasjoner = statistikk.stasjoner.replace(stasjon) { it.stasjonNavn == stasjon.stasjonNavn })
            else statistikk = statistikk.copy(stasjoner = statistikk.stasjoner + stasjon)

            statistikkMap.put(statistikk.partnerNavn, statistikk)
        }

        (statistikkMap.values.toList().sortedBy { it.partnerNavn }).map { it.copy(stasjoner = it.stasjoner.sortedBy { it.stasjonNavn }) }.right()
    }
}