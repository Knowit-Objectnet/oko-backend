package henting.infrastructure.repository

import arrow.core.Either
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.domain.model.StasjonCreateParams
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.domain.params.AvtaleCreateParams
import ombruk.backend.avtale.infrastructure.repository.AvtaleRepository
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.henting.application.api.dto.PlanlagtHentingFindDto
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.domain.params.HenteplanCreateParams
import ombruk.backend.henting.domain.params.PlanlagtHentingCreateParams
import ombruk.backend.henting.infrastructure.repository.HenteplanRepository
import ombruk.backend.henting.infrastructure.repository.PlanlagtHentingRepository
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
internal class PlanlagtHentingRepositoryTest {

    private val testContainer: TestContainer = TestContainer()
    private lateinit var planlagtHentingRepository: PlanlagtHentingRepository
    private lateinit var henteplanRepository: HenteplanRepository
    private lateinit var stasjonRepository: StasjonRepository
    private lateinit var avtaleRepository: AvtaleRepository
    private lateinit var avtale: Avtale
    private lateinit var stasjon: Stasjon
    private lateinit var henteplan: Henteplan
    private lateinit var planlagtHenting1: PlanlagtHenting
    private lateinit var planlagtHenting2: PlanlagtHenting

    @BeforeEach
    fun setUp() {
        testContainer.start()
        planlagtHentingRepository = PlanlagtHentingRepository()
        henteplanRepository = HenteplanRepository()
        stasjonRepository = StasjonRepository()
        avtaleRepository = AvtaleRepository()

        val stasjonParams = object : StasjonCreateParams() {
            override val navn: String = "Teststasjon"
            override val type: StasjonType = StasjonType.GJENBRUK
        }

        transaction {
            val insert = stasjonRepository.insert(stasjonParams)
            require(insert is Either.Right)
            stasjon = insert.b
        }

        val avtaleParams = object: AvtaleCreateParams() {
            override val aktorId: UUID = stasjon.id
            override val type: AvtaleType = AvtaleType.FAST
            override val startDato: LocalDate = LocalDate.now()
            override val sluttDato: LocalDate = LocalDate.now().plusDays(1)
            override val henteplaner: List<HenteplanCreateParams>? = emptyList()
        }

        transaction {
            val insert = avtaleRepository.insert(avtaleParams)
            require(insert is Either.Right)
            avtale = insert.b
        }

        val henteplanParams = object : HenteplanCreateParams() {
            override val avtaleId: UUID = avtale.id
            override val stasjonId: UUID = stasjon.id
            override val frekvens: HenteplanFrekvens = HenteplanFrekvens.UKENTLIG
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusYears(1).plusHours(2)
            override val ukedag: DayOfWeek = DayOfWeek.MONDAY
            override val merknad: String = "Default test Henteplan"
        }

        transaction {
            val insert = henteplanRepository.insert(henteplanParams)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.avtaleId == avtale.id && insert.b.stasjonId == stasjon.id)
            henteplan = insert.b
        }

        val planlagtHentingCreateParams1 = object : PlanlagtHentingCreateParams() {
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusHours(2)
            override val henteplanId: UUID = henteplan.id
        }

        transaction {
            val insert = planlagtHentingRepository.insert(planlagtHentingCreateParams1)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.henteplanId == henteplan.id)
            planlagtHenting1 = insert.b
        }

        val planlagtHentingCreateParams2 = object : PlanlagtHentingCreateParams() {
            override val startTidspunkt: LocalDateTime = LocalDateTime.now().plusHours(2)
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusHours(4)
            override val henteplanId: UUID = henteplan.id
        }

        transaction {
            val insert = planlagtHentingRepository.insert(planlagtHentingCreateParams2)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.henteplanId == henteplan.id)
            planlagtHenting2 = insert.b
        }
    }

    @AfterEach
    fun tearDown() {
        testContainer.stop()
    }

    @Test
    fun insert() {

        val wrongIdParams = object : PlanlagtHentingCreateParams() {
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusHours(2)
            override val henteplanId: UUID = UUID.randomUUID()
        }

        transaction {
            val insert = planlagtHentingRepository.insert(wrongIdParams)
            println(insert)
            require(insert is Either.Left)
            assert(insert.a is RepositoryError.InsertError)
        }

        val correctIdParams = object : PlanlagtHentingCreateParams() {
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusHours(2)
            override val henteplanId: UUID = henteplan.id
        }

        transaction {
            val insert = planlagtHentingRepository.insert(correctIdParams)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.henteplanId == henteplan.id)
            assert(insert.b.avlyst == null)
        }

    }

    @Test
    fun update() {
        //TODO: Lag en updatetest
    }

    @Test
    fun findOne() {
        val wrongId = UUID.randomUUID()
        transaction {
            val findOne = planlagtHentingRepository.findOne(wrongId)
            require(findOne is Either.Left)
            assert(findOne.a is RepositoryError.NoRowsFound)
        }

        transaction {
            val findOne = planlagtHentingRepository.findOne(planlagtHenting1.id)
            println(findOne)
            require(findOne is Either.Right)
            assert(findOne.b == planlagtHenting1)
        }
    }

    @Test
    fun delete() {
        transaction {
            val findPlanlagtHenting = planlagtHentingRepository.findOne(planlagtHenting1.id)
            require(findPlanlagtHenting is Either.Right)
            assert(findPlanlagtHenting.b == planlagtHenting1)
        }

        transaction {
            val deletePlanlagtHenting = planlagtHentingRepository.delete(planlagtHenting1.id)
            assert(deletePlanlagtHenting is Either.Right)
        }

        transaction {
            val findPlanlagtHenting = planlagtHentingRepository.findOne(planlagtHenting1.id)
            require(findPlanlagtHenting is Either.Left)
            assert(findPlanlagtHenting.a is RepositoryError.NoRowsFound)
        }

        transaction {
            val deletePlanlagtHenting = planlagtHentingRepository.delete(planlagtHenting1.id)
            assert(deletePlanlagtHenting is Either.Right)
        }
    }

    @Test
    fun find() {

        transaction {
            val findAll = planlagtHentingRepository.find(PlanlagtHentingFindDto())
            println(findAll)
            require(findAll is Either.Right)
            assert(findAll.b.size == 2)
            assert(findAll.b.contains(planlagtHenting1))
        }

        transaction {
            val findWrongHenteplanId = planlagtHentingRepository.find(PlanlagtHentingFindDto(henteplanId = UUID.randomUUID()))
            require(findWrongHenteplanId is Either.Right)
            assert(findWrongHenteplanId.b.isEmpty())
        }

        transaction {
            val findCorrectHentepanId = planlagtHentingRepository.find(PlanlagtHentingFindDto(henteplanId = henteplan.id))
            println(findCorrectHentepanId)
            require(findCorrectHentepanId is Either.Right)
            assert(findCorrectHentepanId.b.size == 2)
            assert(findCorrectHentepanId.b.contains(planlagtHenting1))
        }

        transaction {
            val findAllBetween = planlagtHentingRepository.find(PlanlagtHentingFindDto(
                after = LocalDateTime.now().minusHours(1),
                before = LocalDateTime.now().plusHours(3)
            ))
            println(findAllBetween)
            require(findAllBetween is Either.Right)
            assert(findAllBetween.b.size == 1)
            assert(findAllBetween.b.contains(planlagtHenting1))
        }

        transaction {
            val findAllBetween = planlagtHentingRepository.find(PlanlagtHentingFindDto(
                after = LocalDateTime.now().minusHours(1),
                before = LocalDateTime.now().plusHours(5)
            ))
            println(findAllBetween)
            require(findAllBetween is Either.Right)
            assert(findAllBetween.b.size == 2)
            assert(findAllBetween.b.containsAll(listOf(planlagtHenting1, planlagtHenting2)))
        }

        transaction {
            val findAllBetween = planlagtHentingRepository.find(PlanlagtHentingFindDto(
                after = LocalDateTime.now().plusHours(1),
                before = LocalDateTime.now().plusHours(5)
            ))
            println(findAllBetween)
            require(findAllBetween is Either.Right)
            assert(findAllBetween.b.size == 1)
            assert(findAllBetween.b.contains(planlagtHenting2))
        }

        transaction {
            val findAllBefore = planlagtHentingRepository.find(PlanlagtHentingFindDto(
                before = LocalDateTime.now().plusHours(1)
            ))
            println(findAllBefore)
            require(findAllBefore is Either.Right)
            assert(findAllBefore.b.isEmpty())
        }
    }
}