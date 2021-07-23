package ombruk.backend.statistikk.application.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import ombruk.backend.henting.application.api.dto.HentingFindDto
import ombruk.backend.henting.application.service.IHentingService
import ombruk.backend.kategori.application.api.dto.KategoriFindDto
import ombruk.backend.kategori.application.service.IKategoriService
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.statistikk.application.api.dto.StatistikkFindDto
import ombruk.backend.statistikk.domain.entity.Kategori
import ombruk.backend.statistikk.domain.entity.Stasjon
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
            it.map {

                it.planlagtHenting?.let { henting ->

                    var statistikkP = statistikkMap.get(henting.aktorId) ?: Statistikk(partnerNavn = henting.aktorNavn)
                    var stasjon = statistikkP.stasjoner.find { stasjon -> stasjon.stasjonNavn == henting.stasjonNavn } ?: Stasjon(henting.stasjonNavn)

                    var vektregistreringerP = henting.vektregistreringer
                    if (dto.kategoriId != null && vektregistreringerP != null) vektregistreringerP = vektregistreringerP.filter { it.kategoriId == dto.kategoriId }
                    vektregistreringerP?.map { registrerting ->

                        val hentingKategori: ombruk.backend.kategori.domain.entity.Kategori
                        val kategoriFinder = kategorier.map { it.find { it.id == registrerting.kategoriId } }
                        require(kategoriFinder is Either.Right)
                        hentingKategori = kategoriFinder.b!!

                        var kategori: Kategori = stasjon.kategorier.find { kategori ->  kategori.kategoriId == registrerting.kategoriId } ?: Kategori(kategoriId = hentingKategori.id, kategoriNavn = hentingKategori.navn)
                        kategori = kategori.copy(vekt = kategori.vekt + registrerting.vekt)

                        if (stasjon.kategorier.find { kategori ->  kategori.kategoriId == registrerting.kategoriId } != null) {
                            val newKategorier = stasjon.kategorier.replace(kategori) {it.kategoriId == kategori.kategoriId}
                            stasjon = stasjon.copy(kategorier = newKategorier)
                        } else stasjon = stasjon.copy(kategorier = stasjon.kategorier + kategori)

                    }

                    if (statistikkP.stasjoner.find { it.stasjonNavn == stasjon.stasjonNavn } != null) {
                        val newStasjoner =
                            statistikkP.stasjoner.replace(stasjon) { it.stasjonNavn == stasjon.stasjonNavn }
                        statistikkP = statistikkP.copy(stasjoner = newStasjoner)
                    } else statistikkP = statistikkP.copy(stasjoner = statistikkP.stasjoner + stasjon)

                    statistikkMap.put(henting.aktorId, statistikkP)
                }

                it.ekstraHenting?.let { henting ->

                    henting.godkjentUtlysning.let {
                        val utlysning = it!!

                        var statistikkE = statistikkMap.get(utlysning.partnerId) ?: Statistikk(partnerNavn = utlysning.partnerNavn)
                        var stasjon = statistikkE.stasjoner.find { stasjon ->  stasjon.stasjonNavn == henting.stasjonNavn } ?: Stasjon(henting.stasjonNavn)

                        var vektregistreringerE = henting.vektregistreringer
                        if (dto.kategoriId != null && vektregistreringerE != null) vektregistreringerE = vektregistreringerE.filter { it.kategoriId == dto.kategoriId }
                        vektregistreringerE?.map { registrerting ->

                            val hentingKategori: ombruk.backend.kategori.domain.entity.Kategori
                            val kategoriFinder = kategorier.map { it.find { it.id == registrerting.kategoriId } }
                            require(kategoriFinder is Either.Right)
                            hentingKategori = kategoriFinder.b!!

                            var kategori: Kategori = stasjon.kategorier.find { kategori ->  kategori.kategoriId == registrerting.kategoriId } ?: Kategori(kategoriId = hentingKategori.id, kategoriNavn = hentingKategori.navn)

                            if (stasjon.kategorier.find { kategori ->  kategori.kategoriId == registrerting.kategoriId } != null) {
                                val newKategorier = stasjon.kategorier.replace(kategori) {it.kategoriId == kategori.kategoriId}
                                stasjon = stasjon.copy(kategorier = newKategorier)
                            } else stasjon = stasjon.copy(kategorier = stasjon.kategorier + kategori)
                        }

                        if (statistikkE.stasjoner.find { it.stasjonNavn == stasjon.stasjonNavn } != null) {
                            val newStasjoner =
                                statistikkE.stasjoner.replace(stasjon) { it.stasjonNavn == stasjon.stasjonNavn }
                            statistikkE = statistikkE.copy(stasjoner = newStasjoner)
                        } else statistikkE = statistikkE.copy(stasjoner = statistikkE.stasjoner + stasjon)

                        statistikkMap.put(utlysning.partnerId, statistikkE)
                    }
                }
            }
        })
        val statistikkList: List<Statistikk> = statistikkMap.values.toList()
        statistikkList.right()
    }
}