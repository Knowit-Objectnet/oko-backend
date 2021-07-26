package ombruk.backend.statistikk.application.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.henting.application.api.dto.HentingFindDto
import ombruk.backend.henting.application.service.IHentingService
import ombruk.backend.kategori.application.api.dto.KategoriFindDto
import ombruk.backend.kategori.application.service.IKategoriService
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.statistikk.application.api.dto.StatistikkFindDto
import ombruk.backend.statistikk.domain.entity.KategoriStatistikk
import ombruk.backend.statistikk.domain.entity.StasjonStatistikk
import ombruk.backend.statistikk.domain.entity.Statistikk
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class StatistikkService(val hentingService: IHentingService, val kategoriService: IKategoriService) : IStatistikkService {
    override fun find(dto: StatistikkFindDto): Either<ServiceError, List<Statistikk>> = transaction {

        fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
            return map {
                if (block(it)) newValue else it
            }
        }

        val hentinger = hentingService.find(HentingFindDto(before = dto.before, after = dto.after, aktorId = dto.partnerId, stasjonId = dto.stasjonId))
        val kategorier = kategoriService.find(KategoriFindDto());
        val statistikkMap = mutableMapOf<UUID, Statistikk>()

        hentinger.fold({it.left()}, {
            it.filter { it.aktorId != null }.map {

                val aktorId = it.aktorId!!
                val aktorNavn = it.aktorNavn!!
                val stasjonNavn = it.stasjonNavn
                var vektregistreringer = it.ekstraHenting?.vektregistreringer ?: it.planlagtHenting?.vektregistreringer ?: emptyList()

                var statistikk = statistikkMap.get(aktorId) ?: Statistikk(partnerNavn = aktorNavn)
                var stasjon = statistikk.stasjoner.find { stasjon -> stasjon.stasjonNavn == stasjonNavn} ?: StasjonStatistikk(stasjonNavn)

                if (dto.kategoriId != null) vektregistreringer = vektregistreringer.filter { it.kategoriId == dto.kategoriId }

                vektregistreringer.map { registrering ->
                    val hentingKategori: Kategori
                    val kategoriFinder = kategorier.map { it.find { it.id == registrering.kategoriId } }
                    require(kategoriFinder is Either.Right)
                    hentingKategori = kategoriFinder.b!!

                    var kategoriStatistikk: KategoriStatistikk = stasjon.kategorier.find { kategori ->  kategori.kategoriId == registrering.kategoriId } ?: KategoriStatistikk(kategoriId = hentingKategori.id, kategoriNavn = hentingKategori.navn)
                    kategoriStatistikk = kategoriStatistikk.copy(vekt = kategoriStatistikk.vekt + registrering.vekt)

                    if (stasjon.kategorier.find { kategori ->  kategori.kategoriId == registrering.kategoriId } != null) {
                        val newKategorier = stasjon.kategorier.replace(kategoriStatistikk) {it.kategoriId == kategoriStatistikk.kategoriId}
                        stasjon = stasjon.copy(kategorier = newKategorier)
                    } else stasjon = stasjon.copy(kategorier = stasjon.kategorier + kategoriStatistikk)
                }

                if (statistikk.stasjoner.find { it.stasjonNavn == stasjon.stasjonNavn } != null) {
                    val newStasjoner =
                        statistikk.stasjoner.replace(stasjon) { it.stasjonNavn == stasjon.stasjonNavn }
                    statistikk = statistikk.copy(stasjoner = newStasjoner)
                } else statistikk = statistikk.copy(stasjoner = statistikk.stasjoner + stasjon)

                statistikkMap.put(aktorId, statistikk)

            }
        })
        statistikkMap.values.toList().right()
    }
}