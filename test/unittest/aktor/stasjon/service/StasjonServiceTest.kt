package aktor.stasjon.service

import arrow.core.Either
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.application.service.StasjonService
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.shared.api.KeycloakGroupIntegration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals


@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PartnerServiceTest {
    private lateinit var stasjonService: StasjonService
    private var stasjonRepository = mockkClass(IStasjonRepository::class)
    private var keycloakGroupIntegration = mockkClass(KeycloakGroupIntegration::class)

    @BeforeEach
    fun setup() {
        stasjonService = StasjonService(stasjonRepository, keycloakGroupIntegration)
    }

    @Test
    internal fun testFindOne(@MockK expected: Stasjon) {
        val id = 1

        every { stasjonRepository.findOne(id) } returns expected.right()

        val actual = stasjonService.findOne(id)
        require(actual is Either.Right)

        assertEquals(expected, actual.b)
    }
}