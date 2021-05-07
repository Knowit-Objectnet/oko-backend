package avtale.service

import arrow.core.Either
import io.mockk.junit5.MockKExtension
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.domain.model.StasjonCreateParams
import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.domain.params.AvtaleCreateParams
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import ombruk.backend.henting.domain.params.HenteplanCreateParams
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AvtaleServiceTest {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun save() {

/*        //Valid aktorId, no Henteplan
        val aktor = object: StasjonCreateParams() {
            override val navn: String = "Testaktør"
            override val type: StasjonType = StasjonType.GJENBRUK
        }

        val aktorInsert = transaction{stasjonRepository.insert(aktor)}
        require(aktorInsert is Either.Right)

        val avtale2 = object: AvtaleCreateParams() {
            override val aktorId: UUID = aktorInsert.b.id
            override val type: AvtaleType = AvtaleType.FAST
            override val startDato: LocalDate = LocalDate.now()
            override val sluttDato: LocalDate = LocalDate.now().plusDays(1)
            override val henteplaner: List<HenteplanCreateParams>? = null
        }

        transaction {
            val insert2 = avtaleRepository.insert(avtale2)
            println(insert2)
            require(insert2 is Either.Right<Avtale>)
            assert(insert2.b.aktorId == avtale2.aktorId)
        }

        //Valid aktorId, with Henteplan
        val henteplaner: List<HenteplanCreateParams> = listOf(
            object : HenteplanCreateParams() {
                override val avtaleId: UUID = UUID.randomUUID()
                override val stasjonId: UUID = aktorInsert.b.id
                override val frekvens: HenteplanFrekvens = HenteplanFrekvens.Ukentlig
                override val startTidspunkt: LocalDateTime = LocalDateTime.now()
                override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusYears(1).plusHours(4)
                override val ukedag: DayOfWeek = DayOfWeek.THURSDAY
                override val merknad: String? = null
            },
            object : HenteplanCreateParams() {
                override val avtaleId: UUID = UUID.randomUUID()
                override val stasjonId: UUID = aktorInsert.b.id
                override val frekvens: HenteplanFrekvens = HenteplanFrekvens.Ukentlig
                override val startTidspunkt: LocalDateTime = LocalDateTime.now()
                override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusYears(2).plusHours(4)
                override val ukedag: DayOfWeek = DayOfWeek.MONDAY
                override val merknad: String? = null
            }
        )

        val avtale3 = object: AvtaleCreateParams() {
            override val aktorId: UUID = aktorInsert.b.id
            override val type: AvtaleType = AvtaleType.FAST
            override val startDato: LocalDate = LocalDate.now()
            override val sluttDato: LocalDate = LocalDate.now().plusDays(1)
            override val henteplaner: List<HenteplanCreateParams>? = henteplaner
        }

        transaction {
            val insert3 = avtaleRepository.insert(avtale3)
            println(insert3)
            require(insert3 is Either.Right<Avtale>)
            assert(insert3.b.aktorId == avtale3.aktorId)
        }*/
    }

    @Test
    fun findOne() {
    }

    @Test
    fun find() {
    }

    @Test
    fun delete() {
    }

    @Test
    fun getAvtaleRepository() {
    }

    @Test
    fun getHentePlanService() {
    }
}