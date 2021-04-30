package ombruk.backend.avtale

import ombruk.backend.avtale.application.service.AvtaleService
import ombruk.backend.avtale.application.service.IAvtaleService
import ombruk.backend.avtale.domain.port.IAvtaleRepository
import ombruk.backend.avtale.infrastructure.repository.AvtaleRepository
import org.koin.dsl.module

val avtaleModule = module(createdAtStart = true) {
    single<IAvtaleRepository> { AvtaleRepository() }
    single<IAvtaleService> { AvtaleService(get(), get()) }
}