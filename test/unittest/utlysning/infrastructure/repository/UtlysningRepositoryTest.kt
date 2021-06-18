package utlysning.infrastructure.repository

import arrow.core.Either
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.domain.model.PartnerCreateParams
import ombruk.backend.aktor.domain.model.StasjonCreateParams
import ombruk.backend.aktor.infrastructure.repository.PartnerRepository
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.params.EkstraHentingCreateParams
import ombruk.backend.henting.infrastructure.repository.EkstraHentingRepository
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.utlysning.application.api.dto.UtlysningFindDto
import ombruk.backend.utlysning.application.api.dto.UtlysningPartnerAcceptDto
import ombruk.backend.utlysning.application.api.dto.UtlysningStasjonAcceptDto
import ombruk.backend.utlysning.application.api.dto.UtlysningUpdateDto
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.domain.params.UtlysningCreateParams
import ombruk.backend.utlysning.infrastructure.repository.UtlysningRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testutils.TestContainer
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class UtlysningRepositoryTest {

    private val testContainer: TestContainer = TestContainer()
    private lateinit var utlysningRepository: UtlysningRepository
    private lateinit var ekstraHentingRepository: EkstraHentingRepository
    private lateinit var stasjonRepository: StasjonRepository
    private lateinit var partnerRepository: PartnerRepository
    private lateinit var stasjon: Stasjon
    private lateinit var partner1: Partner
    private lateinit var partner2: Partner
    private lateinit var ekstraHenting: EkstraHenting
    private lateinit var utlysning1: Utlysning
    private lateinit var utlysning2: Utlysning

    @BeforeEach
    fun setUp() {

        testContainer.start()
        ekstraHentingRepository = EkstraHentingRepository()
        stasjonRepository = StasjonRepository()
        partnerRepository = PartnerRepository()
        utlysningRepository = UtlysningRepository()

        val stasjonParams = object : StasjonCreateParams() {
            override val navn: String = "TestStasjon"
            override val type: StasjonType = StasjonType.GJENBRUK
        }

        val partnerParams1 = object : PartnerCreateParams() {
            override val id: UUID? = null
            override val navn: String = "TestPartner1"
            override val ideell: Boolean = true
        }

        val partnerParams2 = object : PartnerCreateParams() {
            override val id: UUID? = null
            override val navn: String = "TestPartner2"
            override val ideell: Boolean = true
        }

        transaction {
            val insert = stasjonRepository.insert(stasjonParams)
            require(insert is Either.Right)
            stasjon = insert.b
        }

        transaction {
            val insert = partnerRepository.insert(partnerParams1)
            require(insert is Either.Right)
            partner1 = insert.b
        }

        transaction {
            val insert = partnerRepository.insert(partnerParams2)
            require(insert is Either.Right)
            partner2 = insert.b
        }

        val ekstraHentingCreateParams1 = object : EkstraHentingCreateParams() {
            override val stasjonId: UUID = stasjon.id
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusHours(2)
            override val merknad: String = "ABCDEFG"
        }

        transaction {
            val insert = ekstraHentingRepository.insert(ekstraHentingCreateParams1)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.stasjonId == stasjon.id)
            ekstraHenting = insert.b
        }

        val utlysningCreateParams1 = object : UtlysningCreateParams() {
            override val partnerId: UUID = partner1.id
            override val hentingId: UUID = ekstraHenting.id
            override val partnerPameldt: LocalDateTime? = null
            override val stasjonGodkjent: LocalDateTime? = null
            override val partnerSkjult: Boolean = false
            override val partnerVist: Boolean = false
        }

        val utlysningCreateParams2 = object : UtlysningCreateParams() {
            override val partnerId: UUID = partner2.id
            override val hentingId: UUID = ekstraHenting.id
            override val partnerPameldt: LocalDateTime? = null
            override val stasjonGodkjent: LocalDateTime? = null
            override val partnerSkjult: Boolean = false
            override val partnerVist: Boolean = false
        }

        transaction {
            val insert = utlysningRepository.insert(utlysningCreateParams1)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.partnerId == partner1.id)
            utlysning1 = insert.b
        }

        transaction {
            val insert = utlysningRepository.insert(utlysningCreateParams2)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.partnerId == partner2.id)
            utlysning2 = insert.b
        }

    }

    @AfterEach
    fun tearDown() {
        testContainer.stop()
    }

    @Test
    fun insert() {

        val wrongIdParams = object : UtlysningCreateParams() {
            override val partnerId: UUID = UUID.randomUUID()
            override val hentingId: UUID = UUID.randomUUID()
            override val partnerPameldt: LocalDateTime? = null
            override val stasjonGodkjent: LocalDateTime? = null
            override val partnerSkjult: Boolean = false
            override val partnerVist: Boolean = false
        }

        transaction {
            val insert = utlysningRepository.insert(wrongIdParams)
            println(insert)
            require(insert is Either.Left)
            assert(insert.a is RepositoryError.InsertError)
        }

        val correctIdParams = object : UtlysningCreateParams() {
            override val partnerId: UUID = partner1.id
            override val hentingId: UUID = ekstraHenting.id
            override val partnerPameldt: LocalDateTime? = null
            override val stasjonGodkjent: LocalDateTime? = null
            override val partnerSkjult: Boolean = false
            override val partnerVist: Boolean = false
        }

        transaction {
            val insert = utlysningRepository.insert(correctIdParams)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.partnerId == partner1.id)
        }

    }

    @Test
    fun update() {

        val currentDateTime: LocalDateTime = LocalDateTime.now()

        transaction {
            val findUtlysning = utlysningRepository.findOne(utlysning1.id)
            require(findUtlysning is Either.Right)
            assert(findUtlysning.b == utlysning1)
        }

        transaction {
            val updateUtlysning = utlysningRepository.update(
                UtlysningUpdateDto(
                    id = utlysning1.id,
                    partnerPameldt = currentDateTime
                )
            )

            println(updateUtlysning)
            require(updateUtlysning is Either.Right)
            assertTrue(
                (currentDateTime.toEpochSecond(ZoneOffset.UTC)
                        - updateUtlysning.b.partnerPameldt!!.toEpochSecond(ZoneOffset.UTC)) < 1
                , "Dates too far apart!")
        }

        transaction {
            val findUtlysning = utlysningRepository.findOne(utlysning1.id)
            require(findUtlysning is Either.Right)
            assertTrue(
                (currentDateTime.toEpochSecond(ZoneOffset.UTC)
                        - findUtlysning.b.partnerPameldt!!.toEpochSecond(ZoneOffset.UTC)) < 1
                , "Dates too far apart!")
        }

    }

    @Test
    fun findOne() {
        val wrongId = UUID.randomUUID()
        transaction {
            val findOne = utlysningRepository.findOne(wrongId)
            require(findOne is Either.Left)
            assert(findOne.a is RepositoryError.NoRowsFound)
        }

        transaction {
            val findOne = utlysningRepository.findOne(utlysning1.id)
            println(findOne)
            require(findOne is Either.Right)
            assert(findOne.b == utlysning1)
        }
    }

    @Test
    fun partnerAccept() {
        transaction {
            val wrongIdAccept = utlysningRepository.acceptPartner(UtlysningPartnerAcceptDto(UUID.randomUUID(), true))
            require(wrongIdAccept is Either.Left)
            println(wrongIdAccept.a)
            assert(wrongIdAccept.a is RepositoryError.NoRowsFound)
        }

        val currentDateTime: LocalDateTime = LocalDateTime.now()

        transaction {
            val accept = utlysningRepository.acceptPartner(UtlysningPartnerAcceptDto(utlysning1.id, true))
            require(accept is Either.Right)
            assertTrue(
                (currentDateTime.toEpochSecond(ZoneOffset.UTC)
                        - accept.b.partnerPameldt!!.toEpochSecond(ZoneOffset.UTC)) < 1
                , "Dates too far apart!")
        }

        transaction {
            val unAccept = utlysningRepository.acceptPartner(UtlysningPartnerAcceptDto(utlysning1.id, false))
            require(unAccept is Either.Right)
            assertNull(unAccept.b.partnerPameldt)
        }
    }

    @Test
    fun stasjonAccept() {
        transaction {
            val wrongIdAccept = utlysningRepository.acceptStasjon(UtlysningStasjonAcceptDto(UUID.randomUUID(), true))
            require(wrongIdAccept is Either.Left)
            assert(wrongIdAccept.a is RepositoryError.NoRowsFound)
        }

        val currentDateTime: LocalDateTime = LocalDateTime.now()

        transaction {
            val accept = utlysningRepository.acceptStasjon(UtlysningStasjonAcceptDto(utlysning1.id, true))
            require(accept is Either.Right)
            assertTrue(
                (currentDateTime.toEpochSecond(ZoneOffset.UTC)
                        - accept.b.stasjonGodkjent!!.toEpochSecond(ZoneOffset.UTC)) < 1
                , "Dates too far apart!")
        }

        transaction {
            val unAccept = utlysningRepository.acceptStasjon(UtlysningStasjonAcceptDto(utlysning1.id, false))
            require(unAccept is Either.Right)
            assertNull(unAccept.b.stasjonGodkjent)
        }
    }

    @Test
    fun delete() {

        transaction {
            val findUtlysning = utlysningRepository.findOne(utlysning1.id)
            require(findUtlysning is Either.Right)
            assert(findUtlysning.b == utlysning1)
        }

        transaction {
            val deleteUtlysning = utlysningRepository.delete(utlysning1.id)
            assert(deleteUtlysning is Either.Right)
        }

        transaction {
            val findUtlysning = utlysningRepository.findOne(utlysning1.id)
            require(findUtlysning is Either.Left)
            assert(findUtlysning.a is RepositoryError.NoRowsFound)
        }

        transaction {
            val deleteUtlysning = utlysningRepository.delete(utlysning1.id)
            assert(deleteUtlysning is Either.Right)
        }

    }

    @Test
    fun find() {

        transaction {
            val findAll = utlysningRepository.find(UtlysningFindDto())
            println(findAll)
            require(findAll is Either.Right)
            assert(findAll.b.size == 2)
            assert(findAll.b[0] == utlysning1)
        }

        transaction {
            val findWrongHentingId = utlysningRepository.find(UtlysningFindDto(hentingId = UUID.randomUUID()))
            require(findWrongHentingId is Either.Right)
            assert(findWrongHentingId.b.isEmpty())
        }

        transaction {
            val findCorrectHentingId = utlysningRepository.find(UtlysningFindDto(hentingId = ekstraHenting.id))
            println(findCorrectHentingId)
            require(findCorrectHentingId is Either.Right)
            assert(findCorrectHentingId.b.size == 2)
            assert(findCorrectHentingId.b[0] == utlysning1)
        }

        transaction {
            val findCorrectPartnerId = utlysningRepository.find(UtlysningFindDto(partnerId = partner1.id))
            println(findCorrectPartnerId)
            require(findCorrectPartnerId is Either.Right)
            assert(findCorrectPartnerId.b.size == 1)
            assert(findCorrectPartnerId.b[0] == utlysning1)
        }

    }
}