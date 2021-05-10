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
    }

    @Test
    fun findOne() {
    }

    @Test
    fun delete() {
    }

    @Test
    fun find() {
    }
}