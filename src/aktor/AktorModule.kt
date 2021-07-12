package ombruk.backend.aktor

import ombruk.backend.aktor.application.service.*
import ombruk.backend.aktor.domain.port.IKontaktRepository
import ombruk.backend.aktor.domain.port.IPartnerRepository
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.aktor.domain.port.IVerifiseringRepository
import ombruk.backend.aktor.infrastructure.repository.KontaktRepository
import ombruk.backend.aktor.infrastructure.repository.PartnerRepository
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.aktor.infrastructure.repository.VerifiseringRepository
import ombruk.backend.shared.api.KeycloakGroupIntegration
import org.koin.dsl.module

val aktorModule = module(createdAtStart = true) {
    single { KeycloakGroupIntegration() }

    single<IPartnerRepository>     { PartnerRepository() }
    single<IStasjonRepository>     { StasjonRepository() }
    single<IKontaktRepository>     { KontaktRepository() }
    single<IVerifiseringRepository> { VerifiseringRepository() }

    single<IAktorService>       { AktorService(get(), get()) }
    single<IStasjonService>     { StasjonService(get(), get(), get(), get(), get(), get() ) }
    single<IPartnerService>     { PartnerService(get(), get(), get(), get(), get()) }
    single<IVerifiseringService> { VerifiseringService(get()) }
    single<IKontaktService>     { KontaktService(get(), get(), get()) }
}