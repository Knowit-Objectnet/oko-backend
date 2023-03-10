package aktor.aktor

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.aktor.application.service.AktorService
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.enum.AktorType
import ombruk.backend.aktor.domain.port.IPartnerRepository
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.aktor.infrastructure.repository.PartnerRepository
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.shared.error.RepositoryError
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import testutils.mockDatabase
import testutils.unmockDatabase


@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AktorServiceTest {
    private lateinit var aktorService: AktorService

    private var stasjonRepository = mockkClass(StasjonRepository::class)
    private var partnerRepository = mockkClass(PartnerRepository::class)

    @BeforeEach
    fun setup() {
        aktorService = AktorService(stasjonRepository, partnerRepository)
        mockDatabase()
    }

    @AfterEach
    fun tearDown() {
        unmockDatabase()
    }

    @Test
    internal fun testFindOneStasjon(@MockK expected: Stasjon) {
        val id = UUID.randomUUID()

        every { stasjonRepository.findOne(id) } returns expected.right()
        every { expected.id } returns id
        every { expected.navn } returns "StasjonNavn"

        val actual = aktorService.findOne(id)
        require(actual is Either.Right)

        assertNotNull(expected)
        assertEquals(actual.b.aktorType, AktorType.STASJON)
    }

    @Test
    internal fun testFindOnePartner(@MockK expectedStasjon: RepositoryError.NoRowsFound, @MockK expected: Partner) {
        val id = UUID.randomUUID()

        every { stasjonRepository.findOne(id) } returns expectedStasjon.left()
        every { partnerRepository.findOne(id) } returns expected.right()
        every { expected.id } returns id
        every { expected.navn } returns "PartnerNavn"

        val actual = aktorService.findOne(id)
        require(actual is Either.Right)

        assertNotNull(expected)
        assertEquals(actual.b.aktorType, AktorType.PARTNER)
    }
}