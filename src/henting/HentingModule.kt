package ombruk.backend.henting

import ombruk.backend.henting.application.service.*
import ombruk.backend.henting.domain.port.IEkstraHentingRepository
import ombruk.backend.henting.domain.port.IHenteplanRepository
import ombruk.backend.henting.domain.port.IPlanlagtHentingRepository
import ombruk.backend.henting.infrastructure.repository.EkstraHentingRepository
import ombruk.backend.henting.infrastructure.repository.HenteplanRepository
import ombruk.backend.henting.infrastructure.repository.PlanlagtHentingRepository
import org.koin.dsl.module

val hentingModule = module (createdAtStart = true){
    single<IHenteplanRepository> { HenteplanRepository() }
    single<IHenteplanService> { HenteplanService(get(), get()) }
    single<IPlanlagtHentingRepository> { PlanlagtHentingRepository() }
    single<IPlanlagtHentingService> {PlanlagtHentingService(get())}
    single<IEkstraHentingRepository> { EkstraHentingRepository() }
    single<IEkstraHentingService> {EkstraHentingService(get(), get())}
}