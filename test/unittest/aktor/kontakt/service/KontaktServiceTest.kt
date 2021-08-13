package aktor.kontakt.service

import arrow.core.Either
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.application.service.IKontaktService
import ombruk.backend.aktor.application.service.KontaktService
import ombruk.backend.aktor.application.service.StasjonService
import ombruk.backend.aktor.application.service.VerifiseringService
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.entity.VerifiseringStatus
import ombruk.backend.aktor.domain.port.IKontaktRepository
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.notification.application.service.INotificationService
import ombruk.backend.notification.application.service.NotificationService
import ombruk.backend.shared.api.KeycloakGroupIntegration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*
import kotlin.test.assertEquals
import testutils.mockDatabase
import testutils.unmockDatabase


@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PartnerServiceTest {
    private lateinit var kontaktService: KontaktService
    private var kontaktRepository = mockkClass(IKontaktRepository::class)
    private var notificationService = mockkClass(NotificationService::class)
    private var verifiseringService = mockkClass(VerifiseringService::class)

    //TODO Update mockdatabase to have "kontakt"
    @BeforeEach
    fun setup() {
        kontaktService = KontaktService(kontaktRepository, notificationService, verifiseringService)
        mockDatabase()
    }

    @AfterEach
    fun tearDown() {
        unmockDatabase()
    }

    @Test
    internal fun testFindOne(@MockK expected: Kontakt) {
        val id = UUID.randomUUID()

        every { kontaktRepository.findOne(id) } returns expected.right()
        every { expected.id } returns id
        every { verifiseringService.getVerifiseringStatusById(any()) } returns VerifiseringStatus(UUID.randomUUID()).right()
        every { expected.copy(any(), any(), any(), any(), any(), any(), any()) } returns expected

        val actual = kontaktService.getKontaktById(id)
        require(actual is Either.Right)

        assertEquals(expected, actual.b)
    }
}