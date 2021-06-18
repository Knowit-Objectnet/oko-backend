package ombruk.backend.henting

import ombruk.backend.henting.application.service.*
import ombruk.backend.henting.domain.port.IEkstraHentingRepository
import ombruk.backend.henting.domain.port.IHenteplanRepository
import ombruk.backend.henting.domain.port.IPlanlagtHentingRepository
import ombruk.backend.henting.infrastructure.repository.EkstraHentingRepository
import ombruk.backend.henting.infrastructure.repository.HenteplanRepository
import ombruk.backend.henting.infrastructure.repository.PlanlagtHentingRepository
import ombruk.backend.kategori.application.service.EkstraHentingKategoriService
import ombruk.backend.kategori.application.service.IEkstraHentingKategoriService
import ombruk.backend.kategori.domain.port.IEkstraHentingKategoriRepository
import ombruk.backend.kategori.infrastructure.repository.EkstraHentingKategoriRepository
import ombruk.backend.utlysning.application.service.UtlysningService
import org.koin.dsl.module

val hentingModule = module (createdAtStart = true){
    single<IHenteplanRepository> { HenteplanRepository() }
    single<IHenteplanService> { HenteplanService(get(), get(), get()) }
    single<IPlanlagtHentingRepository> { PlanlagtHentingRepository() }
    single<IPlanlagtHentingService> {PlanlagtHentingService(get(), get())}
    single<IEkstraHentingRepository> { EkstraHentingRepository() }
    single<IEkstraHentingKategoriRepository> { EkstraHentingKategoriRepository() }
    single<IEkstraHentingKategoriService> { EkstraHentingKategoriService(get()) }
    single<IEkstraHentingService> {EkstraHentingService(get(), get(), get())}
}