package ombruk.backend.henting

import get
import ombruk.backend.henting.application.service.HenteplanService
import ombruk.backend.henting.application.service.IHenteplanService
import ombruk.backend.henting.domain.port.IHenteplanRepository
import ombruk.backend.henting.infrastructure.HenteplanRepository
import org.koin.dsl.module

val hentingModule = module (createdAtStart = true){
    single<IHenteplanRepository> { HenteplanRepository() }
    single<IHenteplanService> { HenteplanService(get()) }
}