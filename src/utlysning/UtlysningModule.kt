package ombruk.backend.utlysning

import ombruk.backend.utlysning.application.service.IUtlysningService
import ombruk.backend.utlysning.application.service.UtlysningService
import ombruk.backend.utlysning.domain.port.IUtlysningRepository
import ombruk.backend.utlysning.infrastructure.repository.UtlysningRepository
import org.koin.dsl.module

val utlysningModule = module(createdAtStart = true) {
    single<IUtlysningRepository> { UtlysningRepository() }
    single<IUtlysningService> { UtlysningService(get(), get(), get()) }
}