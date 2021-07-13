package henting.application.service

import arrow.core.Either
import arrow.core.right
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import ombruk.backend.henting.application.api.dto.PlanlagtHentingBatchPostDto
import ombruk.backend.henting.application.api.dto.PlanlagtHentingSaveDto
import ombruk.backend.henting.application.service.PlanlagtHentingService
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.infrastructure.repository.PlanlagtHentingRepository
import ombruk.backend.kategori.application.service.HenteplanKategoriService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import testutils.TestContainer
import testutils.mockDatabase
import testutils.unmockDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PlanlagtHentingServiceTest {
    private lateinit var testContainer: TestContainer
    private lateinit var planlagtHentingService: PlanlagtHentingService
    private var henteplanKategoriService = mockkClass(HenteplanKategoriService::class)
    private var planlagtHentingRepository = mockkClass(PlanlagtHentingRepository::class)

    @BeforeEach
    fun setUp() {
        mockDatabase()
        planlagtHentingService = PlanlagtHentingService(planlagtHentingRepository, henteplanKategoriService)
    }

    @AfterEach
    fun tearDown() {
        unmockDatabase()
    }

    @Test
    fun batchCreateForHenteplan(@MockK expected : PlanlagtHenting) {

        val dto = PlanlagtHentingBatchPostDto(
            PlanlagtHentingSaveDto(
                LocalDateTime.of(2021,1,1,10,0),
                LocalDateTime.of(2022,2,1,14,0),
                UUID.randomUUID()
            ),
            listOf(
                LocalDate.of(2021,1,1),
                LocalDate.of(2021,1,8),
                LocalDate.of(2021,1,15),
                LocalDate.of(2021,1,22),
                LocalDate.of(2021,1,29),
            )
        )

        every { planlagtHentingRepository.insert(any()) } returns expected.right()

        val actualList = planlagtHentingService.batchSaveForHenteplan(dto)
        require(actualList is Either.Right)

        assert(actualList.b.size == 5)
        assert(actualList.b.all { it == expected })
    }
}