package aktor.stasjon.service

import arrow.core.Either
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.application.service.KontaktService
import ombruk.backend.aktor.application.service.StasjonService
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.avtale.application.service.AvtaleService
import ombruk.backend.henting.application.service.EkstraHentingService
import ombruk.backend.henting.application.service.HenteplanService
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
    private val henteplanService = mockkClass(HenteplanService::class)
    private val avtaleService = mockkClass(AvtaleService::class)
    private val ekstraHentingService = mockkClass(EkstraHentingService::class)

    @BeforeEach
    fun setup() {
        stasjonService = StasjonService(stasjonRepository, keycloakGroupIntegration, kontaktService, henteplanService, avtaleService, ekstraHentingService)
        mockDatabase()
    }

    @AfterEach
    fun tearDown() {
        unmockDatabase()
    }

    @Test
    internal fun testFindOne(@MockK expected: Stasjon, @MockK expectedKontakt: Kontakt) {
        val id = UUID.randomUUID()

        every { stasjonRepository.findOne(id) } returns expected.right()
        every { kontaktService.getKontakter(any()) } returns Either.right(listOf(expectedKontakt))
        every { expected.id } returns UUID.randomUUID()
        every { expected.navn } returns "TestStasjon"
        every { expected.copy(any(), any(), any(), any()) } returns expected

        val actual = stasjonService.findOne(id, false)
        require(actual is Either.Right)

        assertEquals(expected, actual.b)
    }
}