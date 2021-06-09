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
import ombruk.backend.henting.application.api.dto.HenteplanFindDto
import ombruk.backend.henting.application.api.dto.HenteplanUpdateDto
import ombruk.backend.henting.domain.entity.Henteplan
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.domain.params.HenteplanCreateParams
import ombruk.backend.henting.infrastructure.repository.HenteplanRepository
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*

import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
internal class HenteplanRepositoryTest {

    private val testContainer: TestContainer = TestContainer()
    private lateinit var henteplanRepository: HenteplanRepository
    private lateinit var stasjonRepository: StasjonRepository
    private lateinit var avtaleRepository: AvtaleRepository
    private lateinit var avtale: Avtale
    private lateinit var stasjon: Stasjon
    private lateinit var henteplan: Henteplan
    private lateinit var henteplan2: Henteplan

    @BeforeEach
    fun setUp() {
        testContainer.start()
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

        val createParams = object : HenteplanCreateParams() {
            override val avtaleId: UUID = avtale.id
            override val stasjonId: UUID = stasjon.id
            override val frekvens: HenteplanFrekvens = HenteplanFrekvens.UKENTLIG
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusYears(1).plusHours(2)
            override val ukedag: DayOfWeek = DayOfWeek.MONDAY
            override val merknad: String? = "Default test Henteplan"
        }

        transaction {
            val insert = henteplanRepository.insert(createParams)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.avtaleId == avtale.id && insert.b.stasjonId == stasjon.id)
            henteplan = insert.b
        }

        val createParams2 = object : HenteplanCreateParams() {
            override val avtaleId: UUID = avtale.id
            override val stasjonId: UUID = stasjon.id
            override val frekvens: HenteplanFrekvens = HenteplanFrekvens.UKENTLIG
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusYears(1).plusHours(2)
            override val ukedag: DayOfWeek = DayOfWeek.FRIDAY
            override val merknad: String? = "Default test Henteplan"
        }

        transaction {
            val insert = henteplanRepository.insert(createParams)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.avtaleId == avtale.id && insert.b.stasjonId == stasjon.id)
            henteplan2 = insert.b
        }

    }

    @AfterEach
    fun tearDown() {
        testContainer.stop()
    }
    @Test
    fun insert() {
        val createParams1 = object : HenteplanCreateParams() {
            override val avtaleId: UUID = UUID.randomUUID()
            override val stasjonId: UUID = UUID.randomUUID()
            override val frekvens: HenteplanFrekvens = HenteplanFrekvens.UKENTLIG
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusYears(1).plusHours(2)
            override val ukedag: DayOfWeek = DayOfWeek.MONDAY
            override val merknad: String? = null
        }

        transaction {
            val insert = henteplanRepository.insert(createParams1)
            println(insert)
            require(insert is Either.Left)
        }

        val createParams2 = object : HenteplanCreateParams() {
            override val avtaleId: UUID = avtale.id
            override val stasjonId: UUID = UUID.randomUUID()
            override val frekvens: HenteplanFrekvens = HenteplanFrekvens.UKENTLIG
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusYears(1).plusHours(2)
            override val ukedag: DayOfWeek = DayOfWeek.MONDAY
            override val merknad: String? = null
        }

        transaction {
            val insert = henteplanRepository.insert(createParams2)
            println(insert)
            require(insert is Either.Left)
        }

        val createParams3 = object : HenteplanCreateParams() {
            override val avtaleId: UUID = avtale.id
            override val stasjonId: UUID = stasjon.id
            override val frekvens: HenteplanFrekvens = HenteplanFrekvens.UKENTLIG
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusYears(1).plusHours(2)
            override val ukedag: DayOfWeek = DayOfWeek.MONDAY
            override val merknad: String? = null
        }

        transaction {
            val insert = henteplanRepository.insert(createParams3)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.avtaleId == avtale.id && insert.b.stasjonId == stasjon.id)
        }

    }

    @Test
    fun update() {
        transaction {
            val findHenteplan = henteplanRepository.findOne(henteplan.id)
            require(findHenteplan is Either.Right)
            assert(findHenteplan.b == henteplan)
            assert(findHenteplan.b.ukedag == DayOfWeek.MONDAY)
        }

        transaction {
            val update = henteplanRepository.update(HenteplanUpdateDto(henteplan.id, ukeDag = DayOfWeek.FRIDAY))
            require(update is Either.Right)
            assert(update.b.ukedag == DayOfWeek.FRIDAY)
        }

        transaction {
            val findHenteplan = henteplanRepository.findOne(henteplan.id)
            require(findHenteplan is Either.Right)
            assert(findHenteplan.b.ukedag == DayOfWeek.FRIDAY)
        }
    }

    @Test
    fun findOne() {
        val wrongId = UUID.randomUUID()
        transaction {
            val findOne = henteplanRepository.findOne(wrongId)
            require(findOne is Either.Left)
            assert(findOne.a is RepositoryError.NoRowsFound)
        }

        transaction {
            val findOne = henteplanRepository.findOne(henteplan.id)
            println(findOne)
            require(findOne is Either.Right)
            assert(findOne.b == henteplan)
        }
    }

    @Test
    fun delete() {
        transaction {
            val findHenteplan = henteplanRepository.findOne(henteplan.id)
            require(findHenteplan is Either.Right)
            assert(findHenteplan.b == henteplan)
        }

        transaction {
            val deleteHenteplan = henteplanRepository.delete(henteplan.id)
            assert(deleteHenteplan is Either.Right)
        }

        transaction {
            val findHenteplan = henteplanRepository.findOne(henteplan.id)
            require(findHenteplan is Either.Left)
            assert(findHenteplan.a is RepositoryError.NoRowsFound)
        }

        transaction {
            val deleteHenteplan = henteplanRepository.delete(henteplan.id)
            assert(deleteHenteplan is Either.Right)
        }
    }

    @Test
    fun find() {

        transaction {
            val findAll = henteplanRepository.find(HenteplanFindDto())
            println(findAll)
            require(findAll is Either.Right)
            assert(findAll.b.size == 2)
            assert(findAll.b[0] == henteplan)
        }

        transaction {
            val findWrongAvtaleId = henteplanRepository.find(HenteplanFindDto(avtaleId = UUID.randomUUID()))
            require(findWrongAvtaleId is Either.Right)
            assert(findWrongAvtaleId.b.isEmpty())
        }

        transaction {
            val findCorrectAvtaleId = henteplanRepository.find(HenteplanFindDto(avtaleId = avtale.id))
            require(findCorrectAvtaleId is Either.Right)
            assert(findCorrectAvtaleId.b.size == 2)
            assert(findCorrectAvtaleId.b[0] == henteplan)
        }
    }

    @Test
    fun archiveOne() {
        transaction {
            val archive = henteplanRepository.archive(HenteplanFindDto(id = henteplan2.id))
            require(archive is Either.Right)
        }

        transaction {
            val find = henteplanRepository.find(HenteplanFindDto())
            require(find is Either.Right)
            assert(find.b.size == 1)
            assert(find.b[0] == henteplan)
        }

        transaction {
            val find = henteplanRepository.find(HenteplanFindDto(arkivert = true))
            require(find is Either.Right)
            assert(find.b.size == 2)
            assert(find.b[0] == henteplan)
        }
    }

    @Test
    fun archiveAll() {
        transaction {
            val archive = henteplanRepository.archive(HenteplanFindDto())
            require(archive is Either.Right)
        }

        transaction {
            val find = henteplanRepository.find(HenteplanFindDto())
            require(find is Either.Right)
            assert(find.b.size == 0)
        }

        transaction {
            val find = henteplanRepository.find(HenteplanFindDto(arkivert = true))
            require(find is Either.Right)
            assert(find.b.size == 2)
            assert(find.b[0] == henteplan)
        }
    }
}