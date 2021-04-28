package ombruk.backend.aktor

import ombruk.backend.aktor.application.service.IPartnerService
import ombruk.backend.aktor.application.service.IStasjonService
import ombruk.backend.aktor.application.service.PartnerService
import ombruk.backend.aktor.application.service.StasjonService
import ombruk.backend.aktor.domain.port.IPartnerRepository
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.aktor.infrastructure.repository.PartnerRepository
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.shared.api.KeycloakGroupIntegration
import org.koin.dsl.module

val aktorModule = module(createdAtStart = true) {
    single { KeycloakGroupIntegration() }
    single<IPartnerRepository> { PartnerRepository() }
    single<IStasjonRepository> { StasjonRepository() }

    single<IStasjonService> {StasjonService(get(), get()) }
    single<IPartnerService> {PartnerService(get(), get()) }
}