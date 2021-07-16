package testutils

import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.application.service.*
import ombruk.backend.aktor.domain.entity.Verifisering
import ombruk.backend.aktor.domain.entity.VerifiseringStatus
import ombruk.backend.aktor.domain.port.IKontaktRepository
import ombruk.backend.aktor.domain.port.IPartnerRepository
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.aktor.infrastructure.repository.KontaktRepository
import ombruk.backend.aktor.infrastructure.repository.PartnerRepository
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.notification.application.service.INotificationService
import ombruk.backend.notification.application.service.NotificationService
import ombruk.backend.notification.application.service.SESService
import ombruk.backend.notification.application.service.SNSService
import ombruk.backend.notification.domain.entity.SES
import ombruk.backend.notification.domain.entity.SNS
import ombruk.backend.shared.api.KeycloakGroupIntegration
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.*

@ExtendWith(MockKExtension::class)
class MockAktorModule {

    companion object {

        private var keycloakGroupIntegration = mockkClass(KeycloakGroupIntegration::class)
        private var snsService = mockkClass(SNSService::class)
        private var sesService = mockkClass(SESService::class)
        private var verifiseringService = mockkClass(VerifiseringService::class)

        private fun getWithExpected(@MockK expected: Any): Module {
            every { keycloakGroupIntegration.createGroup(any(), any<UUID>()) } returns expected.right()
            every { keycloakGroupIntegration.deleteGroup(any()) } returns expected.right()
            every { keycloakGroupIntegration.updateGroup(any(), any()) } returns Unit.right()
            every { verifiseringService.verifiser(any()) } returns VerifiseringStatus(UUID.randomUUID(), false, false).right()
            every { verifiseringService.save(any()) } returns Verifisering(UUID.randomUUID()).right()
            every { snsService.sendMessage(any(), any()) } returns SNS(200, "Success")
            every { snsService.sendVerification(any()) } returns SNS(200, "Success")
            every { sesService.sendMessage(any(), any()) } returns SES(200, "Success")
            every { sesService.sendVerification(any()) } returns SES(200, "Success")

            return module {
                single { keycloakGroupIntegration }
                single<IPartnerRepository> { PartnerRepository() }
                single<IStasjonRepository> { StasjonRepository() }
                single<IKontaktRepository> { KontaktRepository() }

                single<IAktorService> { AktorService(get(), get()) }
                single<IStasjonService> { StasjonService(get(), get(), get(), get(), get(), get() ) }
                single<IPartnerService> { PartnerService(get(), get(), get(), get(), get()) }
                single<IKontaktService> { KontaktService(get(), get(), get()) }
                single<INotificationService> { NotificationService(get(), get(), get()) }
                single { snsService }
                single { sesService }
                single<IVerifiseringService> { verifiseringService }
            }
        }

        fun get(): Module {
            return getWithExpected(mockkClass(Any::class))
        }

    }
}