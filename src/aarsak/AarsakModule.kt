package ombruk.backend.aarsak

import ombruk.backend.aarsak.application.service.AarsakService
import ombruk.backend.aarsak.application.service.IAarsakService
import ombruk.backend.aarsak.domain.port.IAarsakRepository
import ombruk.backend.aarsak.infrastructure.repository.AarsakRepository
import ombruk.backend.aktor.application.service.*
import ombruk.backend.aktor.domain.port.IKontaktRepository
import ombruk.backend.aktor.domain.port.IPartnerRepository
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.aktor.infrastructure.repository.KontaktRepository
import ombruk.backend.aktor.infrastructure.repository.PartnerRepository
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.shared.api.KeycloakGroupIntegration
import org.koin.dsl.module

val aarsakModule = module(createdAtStart = true) {
    single<IAarsakRepository> { AarsakRepository() }
    single<IAarsakService> { AarsakService() }
}