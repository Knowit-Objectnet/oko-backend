package aktor.aktor

import arrow.core.Either
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.application.service.AktorService
import ombruk.backend.aktor.application.service.PartnerService
import ombruk.backend.aktor.application.service.StasjonService
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.enum.AktorType
import ombruk.backend.aktor.domain.port.IPartnerRepository
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.shared.api.KeycloakGroupIntegration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.math.exp
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AktorServiceTest {
    private lateinit var aktorService: AktorService

    private var stasjonRepository = mockkClass(IStasjonRepository::class)
    private var partnerRepository = mockkClass(IPartnerRepository::class)
    //private var keycloakGroupIntegration = mockkClass(KeycloakGroupIntegration::class)

    @BeforeEach
    fun setup() {
        aktorService = AktorService(stasjonRepository, partnerRepository)
    }

    @Test
    internal fun testFindOne(@MockK expected: Stasjon) {
        val id = 1

        every { stasjonRepository.findOne(id) } returns expected.right()

        val actual = aktorService.findOne(id)
        require(actual is Either.Right)

        assertNotNull(expected)
        assertEquals(actual.b, AktorType.STASJON)
    }
}