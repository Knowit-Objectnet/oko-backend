package utlysning.application.service

import arrow.core.Either
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.utlysning.application.api.dto.UtlysningBatchSaveDto
import ombruk.backend.utlysning.application.service.UtlysningService
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.infrastructure.repository.UtlysningRepository
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringSaveDto
import ombruk.backend.vektregistrering.application.service.VektregistreringService
import ombruk.backend.vektregistrering.domain.entity.Vektregistrering
import ombruk.backend.vektregistrering.infrastructure.repository.VektregistreringRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import testutils.mockDatabase
import testutils.unmockDatabase
import java.util.*

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class VektregistreringServiceTest {

    private lateinit var vektregistreringService: VektregistreringService
    private var vektregistreringRepository = mockkClass(VektregistreringRepository::class)

    @BeforeEach
    fun setUp() {
        mockDatabase()
        vektregistreringService = VektregistreringService(vektregistreringRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockDatabase()
    }

    @Test
    fun create(@MockK expected: Vektregistrering) {
        val dto = VektregistreringSaveDto(
            hentingId = UUID.randomUUID(),
            kategoriId = UUID.fromString("33812d39-75e9-4ba9-875f-b0724aa68185"), //Kategori: Bøker
            vekt = 100f
        )

        every { vektregistreringRepository.insert(any()) } returns expected.right()
        every { vektregistreringRepository.find(any()) } returns emptyList<Vektregistrering>().right()

        val actual = vektregistreringService.save(dto)
        println(actual)
        require(actual is Either.Right)
        assert(actual.b == expected)

    }
}