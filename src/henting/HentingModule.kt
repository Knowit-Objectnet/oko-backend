package ombruk.backend.henting

import ombruk.backend.henting.application.service.HenteplanService
import ombruk.backend.henting.application.service.IHenteplanService
import ombruk.backend.henting.application.service.IPlanlagtHentingService
import ombruk.backend.henting.application.service.PlanlagtHentingService
import ombruk.backend.henting.domain.port.IHenteplanRepository
import ombruk.backend.henting.domain.port.IPlanlagtHentingRepository
import ombruk.backend.henting.infrastructure.repository.HenteplanRepository
import ombruk.backend.henting.infrastructure.repository.PlanlagtHentingRepository
import org.koin.dsl.module

val hentingModule = module (createdAtStart = true){
    single<IHenteplanRepository> { HenteplanRepository() }
    single<IHenteplanService> { HenteplanService(get()) }
    single<IPlanlagtHentingRepository> { PlanlagtHentingRepository() }
    single<IPlanlagtHentingService> {PlanlagtHentingService(get())}
}