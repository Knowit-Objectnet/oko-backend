package aktor.stasjon.service

import arrow.core.Either
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.kategori.application.api.dto.KategoriFindDto
import ombruk.backend.kategori.application.api.dto.KategoriSaveDto
import ombruk.backend.kategori.application.service.KategoriService
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.kategori.domain.port.IKategoriRepository
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
internal class KategoriServiceTest {
    private lateinit var kategoriService: KategoriService
    private var kategoriRepository = mockkClass(IKategoriRepository::class)

    @BeforeEach
    fun setup() {
        kategoriService = KategoriService(kategoriRepository)
        mockDatabase()
    }

    @AfterEach
    fun tearDown() {
        unmockDatabase()
    }

    @Test
    fun testFindOne(@MockK expected: Kategori) {
        val id = UUID.randomUUID()

        every { kategoriRepository.findOne(id) } returns expected.right()

        val actual = kategoriService.findOne(id)
        require(actual is Either.Right)

        assertEquals(expected, actual.b)
    }

    @Test
    fun testFind(@MockK expected: List<Kategori>) {
        val dto = KategoriFindDto(
            UUID.randomUUID(),
            "Test"
        )

        every { kategoriRepository.find(dto) } returns expected.right()

        val actual = kategoriService.find(dto)
        require(actual is Either.Right)

        assertEquals(expected, actual.b)
    }

    @Test
    fun testInsert(@MockK expected: Kategori) {
        val dto = KategoriSaveDto("Test")

        every { kategoriRepository.insert(dto) } returns expected.right()

        val actual = kategoriService.save(dto)
        require(actual is Either.Right)

        assertEquals(expected, actual.b)
    }
}