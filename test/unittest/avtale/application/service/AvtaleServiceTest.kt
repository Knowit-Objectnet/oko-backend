package avtale.application.service

import arrow.core.Either
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.avtale.application.api.dto.AvtaleFindDto
import ombruk.backend.avtale.application.service.AvtaleService
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.infrastructure.repository.AvtaleRepository
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.henting.application.service.HenteplanService
import ombruk.backend.henting.domain.entity.Henteplan
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import testutils.mockDatabase
import testutils.unmockDatabase
import java.time.LocalDate
import java.util.*

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AvtaleServiceTest {
    private lateinit var avtaleService: AvtaleService
    private val avtaleRepository = mockkClass(AvtaleRepository::class)
    private val henteplanService = mockkClass(HenteplanService::class)

    private val avtale1 = Avtale(
        UUID.randomUUID(),
        UUID.randomUUID(),
        AvtaleType.FAST,
        LocalDate.of(2021,1,1),
        LocalDate.of(2022,1,1),
        emptyList()
    )

    private val avtale2 = Avtale(
        UUID.randomUUID(),
        UUID.randomUUID(),
        AvtaleType.ANNEN,
        LocalDate.of(2021,2,1),
        LocalDate.of(2022,2,1),
        emptyList()
    )

    @BeforeEach
    fun setUp() {
        mockDatabase()
        avtaleService = AvtaleService(avtaleRepository, henteplanService)
    }

    @AfterEach
    fun tearDown() {
        unmockDatabase()
    }

    @Test
    fun save() {

        //Test if batch-creating henteplan also works

    }

    @Test
    fun findOne(@MockK expectedPlans : List<Henteplan>) {

        every { henteplanService.findAllForAvtale(avtale1.id) } returns expectedPlans.right()
        every { avtaleRepository.findOne(avtale1.id) } returns avtale1.right()

        val actual = avtaleService.findOne(avtale1.id)
        require(actual is Either.Right)
        assertEquals(avtale1.id, actual.b.id)
        assertEquals(expectedPlans, actual.b.henteplaner)
    }

    @Test
    fun find(@MockK expectedPlans1 : List<Henteplan>, @MockK expectedPlans2 : List<Henteplan>) {
        every { henteplanService.findAllForAvtale(avtale1.id) } returns expectedPlans1.right()
        every { henteplanService.findAllForAvtale(avtale2.id) } returns expectedPlans2.right()

        every { avtaleRepository.find(AvtaleFindDto()) } returns Either.right(listOf(avtale1, avtale2))
        every { avtaleRepository.find(AvtaleFindDto(id = avtale1.id)) } returns Either.right(listOf(avtale1))
        every { avtaleRepository.find(AvtaleFindDto(id = avtale2.id)) } returns Either.right(listOf(avtale2))

        //FIXME: Write case where none should be returned
//        every { avtaleRepository.find(AvtaleFindDto(id = and(not(null), not(or(avtale1.id, avtale2.id))))) } returns Either.right(
//            emptyList())


        val actual = avtaleService.find(AvtaleFindDto())
        val actual1 = avtaleService.find(AvtaleFindDto(id = avtale1.id))
        val actual2 = avtaleService.find(AvtaleFindDto(id = avtale2.id))

        require(actual is Either.Right)
        require(actual1 is Either.Right)
        require(actual2 is Either.Right)

        assert(actual1.b.size == 1)
        assertEquals(avtale1.id, actual1.b[0].id)
        assertEquals(expectedPlans1 , actual1.b[0].henteplaner)

        assert(actual2.b.size == 1)
        assertEquals(avtale2.id, actual2.b[0].id)
        assertEquals(expectedPlans2 , actual2.b[0].henteplaner)

        assert(actual.b.size == 2)
        assert(actual.b.containsAll(listOf(actual1.b[0], actual2.b[0])))

    }

    @Test
    fun delete() {
    }
}