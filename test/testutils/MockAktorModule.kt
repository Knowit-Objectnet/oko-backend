package testutils

import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.application.service.*
import ombruk.backend.aktor.domain.port.IKontaktRepository
import ombruk.backend.aktor.domain.port.IPartnerRepository
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.aktor.infrastructure.repository.KontaktRepository
import ombruk.backend.aktor.infrastructure.repository.PartnerRepository
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.shared.api.KeycloakGroupIntegration
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.*

@ExtendWith(MockKExtension::class)
class MockAktorModule {

    companion object {

        private var keycloakGroupIntegration = mockkClass(KeycloakGroupIntegration::class)

        private fun getWithExpected(@MockK expected: Any): Module {
            every { keycloakGroupIntegration.createGroup(any(), any<UUID>()) } returns expected.right()
            every { keycloakGroupIntegration.deleteGroup(any()) } returns expected.right()
            every { keycloakGroupIntegration.updateGroup(any(), any()) } returns Unit.right()
            return module {
                single { keycloakGroupIntegration }
                single<IPartnerRepository> { PartnerRepository() }
                single<IStasjonRepository> { StasjonRepository() }
                single<IKontaktRepository> { KontaktRepository() }

                single<IAktorService> { AktorService(get(), get()) }
                single<IStasjonService> { StasjonService(get(), get(), get(), get(), get(), get() ) }
                single<IPartnerService> { PartnerService(get(), get(), get(), get(), get()) }
                single<IKontaktService> { KontaktService(get()) }
            }
        }

        fun get(): Module {
            return getWithExpected(mockkClass(Any::class))
        }

    }
}