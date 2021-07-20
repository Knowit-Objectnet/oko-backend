package avtale.infrastructure.repository

import arrow.core.Either
import ombruk.backend.avtale.application.api.dto.AvtaleFindDto
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.domain.params.AvtaleCreateParams
import ombruk.backend.avtale.domain.params.AvtaleUpdateParams
import ombruk.backend.avtale.infrastructure.repository.AvtaleRepository
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.henting.domain.params.HenteplanCreateParams
import ombruk.backend.henting.infrastructure.repository.HenteplanRepository
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import java.time.LocalDate
import java.util.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class AvtaleRepositoryTest {
    private val testContainer: TestContainer = TestContainer()
    private lateinit var avtaleRepository: AvtaleRepository
    private lateinit var henteplanRepository: HenteplanRepository
    private lateinit var avtale: Avtale

    @BeforeEach
    fun setUp() {
        testContainer.start()
        avtaleRepository = AvtaleRepository()
        henteplanRepository = HenteplanRepository()

        val avtaleParams = object: AvtaleCreateParams() {
            override val aktorId: UUID = UUID.randomUUID()
            override val type: AvtaleType = AvtaleType.FAST
            override val startDato: LocalDate = LocalDate.now()
            override val sluttDato: LocalDate = LocalDate.now().plusDays(1)
            override val henteplaner: List<HenteplanCreateParams>? = emptyList()
            override val merknad: String? = null
        }

        transaction {
            val insert = avtaleRepository.insert(avtaleParams)
            require(insert is Either.Right)
            avtale = insert.b
        }
    }

    @AfterEach
    fun tearDown() {
        testContainer.stop()
    }

    @Test
    fun insert() {

        val avtale = object: AvtaleCreateParams() {
            override val aktorId: UUID = UUID.randomUUID()
            override val type: AvtaleType = AvtaleType.FAST
            override val startDato: LocalDate = LocalDate.now()
            override val sluttDato: LocalDate = LocalDate.now().plusDays(1)
            override val henteplaner: List<HenteplanCreateParams>? = emptyList()
            override val merknad: String? = null
        }

        transaction {
            val insert = avtaleRepository.insert(avtale)
            println(insert)
            assert(insert is Either.Right)
        }

    }

    @Test
    fun update() {
        //TODO: Implement
        val find = transaction { avtaleRepository.findOne(avtale.id) }
        require(find is Either.Right<Avtale>)
        assert(find.b == avtale)

        val updateParams = object: AvtaleUpdateParams() {
            override val id: UUID = avtale.id
            override val startDato: LocalDate = LocalDate.now().plusDays(2)
            override val sluttDato: LocalDate = LocalDate.now().plusDays(10)
            override val type: AvtaleType? = null
            override val merknad: String? = null
        }
        val update = transaction { avtaleRepository.update(updateParams) }
        require(update is Either.Right<Avtale>)
        assert(update.b.id == find.b.id)
        assert(update.b != find.b)
    }

    @Test
    fun findOne() {

        val find = transaction { avtaleRepository.findOne(avtale.id) }
        require(find is Either.Right<Avtale>)
        assert(find.b == avtale)

        val find2 = transaction { avtaleRepository.findOne(UUID.randomUUID()) }
        require(find2 is Either.Left)
        assert(find2.a is RepositoryError.NoRowsFound)
    }

    @Test
    fun find() {
        val allAvtale = transaction { avtaleRepository.find(AvtaleFindDto())}
        require(allAvtale is Either.Right)
        assert(allAvtale.b.isNotEmpty())
        val firstAvtale = allAvtale.b[0]

        val specificAvtale = transaction { avtaleRepository.find(AvtaleFindDto(aktorId = firstAvtale.aktorId)) }
        require(specificAvtale is Either.Right)
        assert(specificAvtale.b.isNotEmpty())
        assert(specificAvtale.b.contains(firstAvtale))

        val nonExistingAvtale = transaction { avtaleRepository.find(AvtaleFindDto(aktorId = UUID.randomUUID())) }
        require(nonExistingAvtale is Either.Right)
        require(nonExistingAvtale.b.isEmpty())
    }

    @Test
    fun delete() {

        //Find an existing Avtale
        val allAvtale = transaction { avtaleRepository.find(AvtaleFindDto())}
        println(allAvtale)
        require(allAvtale is Either.Right)
        assert(allAvtale.b.isNotEmpty())
        val firstAvtale = allAvtale.b[0]

        //Delete the found Avtale
        val delete = transaction { avtaleRepository.delete(firstAvtale.id) }
        println(delete)
        assert(delete is Either.Right)

        //Confirm that it is deleted
        val sameAvtale = transaction { avtaleRepository.find(AvtaleFindDto(firstAvtale.id))}
        require(sameAvtale is Either.Right)
        assert(sameAvtale.b.isEmpty())

        //FIXME: Is this the intended behavior?
        //Check that Either.Right is returned also when trying to delete a non-existing Avtale
        val delete2 = transaction { avtaleRepository.delete(firstAvtale.id) }
        println(delete2)
        assert(delete2 is Either.Right)
    }
}