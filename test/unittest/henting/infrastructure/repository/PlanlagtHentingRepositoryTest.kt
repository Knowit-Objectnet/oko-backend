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
import ombruk.backend.henting.application.api.dto.PlanlagtHentingUpdateDto
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.domain.params.HenteplanCreateParams
import ombruk.backend.henting.domain.params.PlanlagtHentingCreateParams
import ombruk.backend.henting.infrastructure.repository.HenteplanRepository
import ombruk.backend.henting.infrastructure.repository.PlanlagtHentingRepository
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.transactions.transaction
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

    private lateinit var testContainer: TestContainer
    private lateinit var planlagtHentingRepository: PlanlagtHentingRepository
    private lateinit var henteplanRepository: HenteplanRepository
    private lateinit var stasjonRepository: StasjonRepository
    private lateinit var avtaleRepository: AvtaleRepository
    private lateinit var avtale: Avtale
    private lateinit var stasjon: Stasjon
    private lateinit var henteplan: Henteplan
    private lateinit var planlagtHenting: PlanlagtHenting

    @BeforeEach
    fun setUp() {
        testContainer = TestContainer()
        planlagtHentingRepository = PlanlagtHentingRepository()
        henteplanRepository = HenteplanRepository()
        stasjonRepository = StasjonRepository()
        avtaleRepository = AvtaleRepository()

        val avtaleParams = object: AvtaleCreateParams() {
            override val aktorId: UUID = UUID.randomUUID()
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

        val stasjonParams = object : StasjonCreateParams() {
            override val navn: String = "Grefsen"
            override val type: StasjonType = StasjonType.GJENBRUK
        }

        transaction {
            val insert = stasjonRepository.insert(stasjonParams)
            require(insert is Either.Right)
            stasjon = insert.b
        }

        val henteplanParams = object : HenteplanCreateParams() {
            override val avtaleId: UUID = avtale.id
            override val stasjonId: UUID = stasjon.id
            override val frekvens: HenteplanFrekvens = HenteplanFrekvens.Ukentlig
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusYears(1).plusHours(2)
            override val ukedag: DayOfWeek = DayOfWeek.MONDAY
            override val merknad: String? = "Default test Henteplan"
        }

        transaction {
            val insert = henteplanRepository.insert(henteplanParams)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.avtaleId == avtale.id && insert.b.stasjonId == stasjon.id)
            henteplan = insert.b
        }

        val planlagtHentingCreateParams = object : PlanlagtHentingCreateParams() {
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusHours(2)
            override val merknad: String? = null
            override val henteplanId: UUID = henteplan.id
            override val avlyst: LocalDateTime? = null
        }

        transaction {
            val insert = planlagtHentingRepository.insert(planlagtHentingCreateParams)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.henteplanId == henteplan.id)
            planlagtHenting = insert.b
        }
    }

    @Test
    fun insert() {

        val wrongIdParams = object : PlanlagtHentingCreateParams() {
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusHours(2)
            override val merknad: String? = null
            override val henteplanId: UUID = UUID.randomUUID()
            override val avlyst: LocalDateTime? = null
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
            override val merknad: String? = null
            override val henteplanId: UUID = henteplan.id
            override val avlyst: LocalDateTime? = null
        }

        transaction {
            val insert = planlagtHentingRepository.insert(correctIdParams)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.henteplanId == henteplan.id)
        }

    }

    @Test
    fun update() {

        transaction {
            val findHenting = planlagtHentingRepository.findOne(planlagtHenting.id)
            require(findHenting is Either.Right)
            assert(findHenting.b == planlagtHenting)
            assert(findHenting.b.avlyst == null)
        }

        transaction {
            val update = planlagtHentingRepository.update(PlanlagtHentingUpdateDto(id=planlagtHenting.id,avlyst = LocalDateTime.of(2021,5,10,12,0)))
            require(update is Either.Right)
            assert(update.b.avlyst == LocalDateTime.of(2021,5,10,12,0))
        }

        transaction {
            val findHenting = planlagtHentingRepository.findOne(planlagtHenting.id)
            require(findHenting is Either.Right)
            assert(findHenting.b.avlyst == LocalDateTime.of(2021,5,10,12,0))
        }

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
            val findOne = planlagtHentingRepository.findOne(planlagtHenting.id)
            println(findOne)
            require(findOne is Either.Right)
            assert(findOne.b == planlagtHenting)
        }
    }

    @Test
    fun delete() {
        transaction {
            val findPlanlagtHenting = planlagtHentingRepository.findOne(planlagtHenting.id)
            require(findPlanlagtHenting is Either.Right)
            assert(findPlanlagtHenting.b == planlagtHenting)
        }

        transaction {
            val deletePlanlagtHenting = planlagtHentingRepository.delete(planlagtHenting.id)
            assert(deletePlanlagtHenting is Either.Right)
        }

        transaction {
            val findPlanlagtHenting = planlagtHentingRepository.findOne(planlagtHenting.id)
            require(findPlanlagtHenting is Either.Left)
            assert(findPlanlagtHenting.a is RepositoryError.NoRowsFound)
        }

        transaction {
            val deletePlanlagtHenting = planlagtHentingRepository.delete(planlagtHenting.id)
            assert(deletePlanlagtHenting is Either.Right)
        }
    }

    @Test
    fun find() {

        //TODO: This currently doesn't work, as TestContainer does not reset between tests
/*        transaction {
            val findAll = planlagtHentingRepository.find(PlanlagtHentingFindDto())
            println(findAll)
            require(findAll is Either.Right)
            assert(findAll.b.size == 1)
            assert(findAll.b[0] == planlagtHenting)
        }*/

        transaction {
            val findWrongHenteplanId = planlagtHentingRepository.find(PlanlagtHentingFindDto(henteplanId = UUID.randomUUID()))
            require(findWrongHenteplanId is Either.Right)
            assert(findWrongHenteplanId.b.isEmpty())
        }

        transaction {
            val findCorrectHentepanId = planlagtHentingRepository.find(PlanlagtHentingFindDto(henteplanId = henteplan.id))
            require(findCorrectHentepanId is Either.Right)
            assert(findCorrectHentepanId.b.size == 1)
            assert(findCorrectHentepanId.b[0] == planlagtHenting)
        }
    }
}