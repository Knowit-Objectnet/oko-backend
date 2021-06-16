package henting.application.service

import arrow.core.Either
import arrow.core.right
import henting.application.api.dto.HenteplanSaveDto
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.henting.application.api.dto.HenteplanFindDto
import ombruk.backend.henting.application.service.HenteplanService
import ombruk.backend.henting.application.service.PlanlagtHentingService
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.entity.PlanlagtHentingWithParents
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.infrastructure.repository.HenteplanRepository
import ombruk.backend.kategori.application.service.HenteplanKategoriService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import testutils.mockDatabase
import testutils.unmockDatabase
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class HenteplanServiceTest {
    private lateinit var henteplanService: HenteplanService
    private val henteplanRepository = mockkClass(HenteplanRepository::class)
    private val planlagtHentingService: PlanlagtHentingService = mockkClass(PlanlagtHentingService::class)
    private val henteplanKategoriService = mockkClass(HenteplanKategoriService::class)

    private lateinit var henteplanPostDto : HenteplanSaveDto
    private lateinit var henteplan : Henteplan

    @BeforeEach
    fun setUp() {
        mockDatabase()
        henteplanService = HenteplanService(henteplanRepository, planlagtHentingService, henteplanKategoriService)

        henteplanPostDto = HenteplanSaveDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            HenteplanFrekvens.UKENTLIG,
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
            emptyList(),
            emptyList()
        )
    }

    @AfterEach
    fun tearDown() {
        unmockDatabase()
    }

    @Test
    fun createPlanlagtHentinger(@MockK expected : List<PlanlagtHentingWithParents>) {

        every { planlagtHentingService.batchSaveForHenteplan(any()) } returns expected.right()
        val actual = henteplanService.createPlanlagtHentinger(henteplanPostDto, henteplan.id)
        assertEquals(expected.right(), actual)

    }

    @Test
    fun appendPlanlagtHentinger(@MockK expectedList : List<PlanlagtHentingWithParents>) {

        every { planlagtHentingService.batchSaveForHenteplan(any()) } returns expectedList.right()

        val actual = henteplanService.appendPlanlagtHentinger(henteplanPostDto, henteplan.id, henteplan)

        require(actual is Either.Right)
        assertEquals(henteplan.id, actual.b.id)
        assertEquals(expectedList, actual.b.planlagteHentinger)
    }

    @Test
    fun create(@MockK expectedList : List<PlanlagtHentingWithParents>) {
        every { henteplanRepository.insert(any()) } returns henteplan.right()
        every { planlagtHentingService.batchSaveForHenteplan(any()) } returns expectedList.right()

        val actual = henteplanService.save(henteplanPostDto)

        require(actual is Either.Right)
        assertEquals(henteplan.id, actual.b.id)
        assertEquals(expectedList, actual.b.planlagteHentinger)

    }

    @Test
    fun batchCreate(@MockK expectedList : List<PlanlagtHentingWithParents>) {
        every { henteplanRepository.insert(any()) } returns henteplan.right()
        every { planlagtHentingService.batchSaveForHenteplan(any()) } returns expectedList.right()

        val actual = henteplanService.batchSave(listOf(henteplanPostDto, henteplanPostDto))

        require(actual is Either.Right)
        assertTrue(actual.b.size == 2)
        assertEquals(henteplan.id, actual.b[0].id)
        assertEquals(expectedList, actual.b[0].planlagteHentinger)
        assertEquals(actual.b[0], actual.b[1])
    }

    @Test
    fun archiveOne(@MockK expectedUnit: Unit) {
        every { planlagtHentingService.archive(any())} returns expectedUnit.right()
        every { henteplanKategoriService.archive(any())} returns expectedUnit.right()
        every { henteplanRepository.archiveOne(any()) } returns henteplan.right()
        val actual = henteplanService.archiveOne(henteplan.id)
        assertEquals(expectedUnit.right(), actual)
    }

    @Test
    fun archive(@MockK expectedUnit: Unit) {
        every { planlagtHentingService.archive(any())} returns expectedUnit.right()
        every { henteplanKategoriService.archive(any())} returns expectedUnit.right()
        every { henteplanRepository.archive(any()) } returns listOf(henteplan).right()
        val actual = henteplanService.archive(HenteplanFindDto(id = henteplan.id))
        assertEquals(Either.Right(Unit), actual)
    }

}