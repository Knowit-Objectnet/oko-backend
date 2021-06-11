package aktor.stasjon.service

import arrow.core.Either
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.application.service.KontaktService
import ombruk.backend.aktor.application.service.StasjonService
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.port.IStasjonRepository
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
    private lateinit var stasjonService: StasjonService
    private var stasjonRepository = mockkClass(IStasjonRepository::class)
    private var keycloakGroupIntegration = mockkClass(KeycloakGroupIntegration::class)
    private var kontaktService = mockkClass(KontaktService::class)

    @BeforeEach
    fun setup() {
        stasjonService = StasjonService(stasjonRepository, keycloakGroupIntegration, kontaktService)
        mockDatabase()
    }

    @AfterEach
    fun tearDown() {
        unmockDatabase()
    }

    @Test
    internal fun testFindOne(@MockK expected: Stasjon) {
        val id = UUID.randomUUID()

        every { stasjonRepository.findOne(id) } returns expected.right()

        val actual = stasjonService.findOne(id)
        require(actual is Either.Right)

        assertEquals(expected, actual.b)
    }
}