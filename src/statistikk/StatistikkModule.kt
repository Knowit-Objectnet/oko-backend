package ombruk.backend.statistikk

import ombruk.backend.statistikk.application.service.IStatistikkService
import ombruk.backend.statistikk.application.service.StatistikkService
import org.koin.dsl.module

val statistikkModule = module(createdAtStart = true) {
    single<IStatistikkService> { StatistikkService() }
}