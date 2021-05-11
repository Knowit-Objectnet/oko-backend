package henting.application.service

import arrow.core.Either
import arrow.core.right
import henting.application.api.dto.HenteplanPostDto
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.henting.application.service.HenteplanService
import ombruk.backend.henting.application.service.PlanlagtHentingService
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.infrastructure.repository.HenteplanRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*

//TODO: Make work without TestContainer

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
internal class HenteplanServiceTest {
    private lateinit var testContainer: TestContainer
    private lateinit var henteplanService: HenteplanService
    private val henteplanRepository = mockkClass(HenteplanRepository::class)
    private val planlagtHentingService: PlanlagtHentingService = mockkClass(PlanlagtHentingService::class)

    private lateinit var henteplanPostDto : HenteplanPostDto
    private lateinit var henteplan : Henteplan

    @BeforeEach
    fun setUp() {
        testContainer = TestContainer()
        henteplanService = HenteplanService(henteplanRepository, planlagtHentingService)

        henteplanPostDto = HenteplanPostDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            HenteplanFrekvens.Ukentlig,
            LocalDateTime.of(2021,1,1,10,0),
            LocalDateTime.of(2021,2,1,14,0),
            DayOfWeek.FRIDAY,
            null
        )

        henteplan = Henteplan(
            UUID.randomUUID(),
            henteplanPostDto.avtaleId,
            henteplanPostDto.stasjonId,
            henteplanPostDto.frekvens,
            henteplanPostDto.startTidspunkt,
            henteplanPostDto.sluttTidspunkt,
            henteplanPostDto.ukedag,
            henteplanPostDto.merknad,
            emptyList()
        )
    }

    @Test
    fun createPlanlagtHentinger(@MockK expected : List<PlanlagtHenting>) {

        every { planlagtHentingService.batchCreateForHenteplan(any()) } returns expected.right()
        val actual = henteplanService.createPlanlagtHentinger(henteplanPostDto, henteplan.id)
        assertEquals(expected.right(), actual)

    }

    @Test
    fun appendPlanlagtHentinger(@MockK expectedList : List<PlanlagtHenting>) {

        every { planlagtHentingService.batchCreateForHenteplan(any()) } returns expectedList.right()

        val actual = henteplanService.appendPlanlagtHentinger(henteplanPostDto, henteplan.id, henteplan)

        require(actual is Either.Right)
        assertEquals(henteplan.id, actual.b.id)
        assertEquals(expectedList, actual.b.planlagteHentinger)
    }

    @Test
    fun create(@MockK expectedList : List<PlanlagtHenting>) {
        every { henteplanRepository.insert(any()) } returns henteplan.right()
        every { planlagtHentingService.batchCreateForHenteplan(any()) } returns expectedList.right()

        val actual = henteplanService.create(henteplanPostDto)

        require(actual is Either.Right)
        assertEquals(henteplan.id, actual.b.id)
        assertEquals(expectedList, actual.b.planlagteHentinger)

    }

    @Test
    fun batchCreate(@MockK expectedList : List<PlanlagtHenting>) {
        every { henteplanRepository.insert(any()) } returns henteplan.right()
        every { planlagtHentingService.batchCreateForHenteplan(any()) } returns expectedList.right()

        val actual = henteplanService.batchCreate(listOf(henteplanPostDto, henteplanPostDto))

        require(actual is Either.Right)
        assertTrue(actual.b.size == 2)
        assertEquals(henteplan.id, actual.b[0].id)
        assertEquals(expectedList, actual.b[0].planlagteHentinger)
        assertEquals(actual.b[0], actual.b[1])
    }

}