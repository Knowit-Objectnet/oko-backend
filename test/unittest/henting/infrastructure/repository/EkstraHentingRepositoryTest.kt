package henting.infrastructure.repository

import arrow.core.Either
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.enum.StasjonType
import ombruk.backend.aktor.domain.model.PartnerCreateParams
import ombruk.backend.aktor.domain.model.StasjonCreateParams
import ombruk.backend.aktor.infrastructure.repository.PartnerRepository
import ombruk.backend.aktor.infrastructure.repository.StasjonRepository
import ombruk.backend.henting.application.api.dto.EkstraHentingFindDto
import ombruk.backend.henting.application.api.dto.EkstraHentingUpdateDto
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.params.EkstraHentingCreateParams
import ombruk.backend.henting.infrastructure.repository.EkstraHentingRepository
import ombruk.backend.kategori.application.api.dto.EkstraHentingKategoriBatchSaveDto
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testutils.TestContainer
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

internal class EkstraHentingRepositoryTest {

    private val testContainer: TestContainer = TestContainer()
    private lateinit var ekstraHentingRepository: EkstraHentingRepository
    private lateinit var stasjonRepository: StasjonRepository
    private lateinit var partnerRepository: PartnerRepository
    private lateinit var stasjon: Stasjon
    private lateinit var partner: Partner
    private lateinit var ekstraHenting1: EkstraHenting
    private lateinit var ekstraHenting2: EkstraHenting


    @BeforeEach
    fun setUp() {
        testContainer.start()
        ekstraHentingRepository = EkstraHentingRepository()
        stasjonRepository = StasjonRepository()
        partnerRepository = PartnerRepository()

        val stasjonParams = object : StasjonCreateParams() {
            override val navn: String = "TestStasjon"
            override val type: StasjonType = StasjonType.GJENBRUK
        }

        val partnerParams = object : PartnerCreateParams() {
            override val id: UUID? = null
            override val navn: String = "TestPartner"
            override val ideell: Boolean = true
        }

        transaction {
            val insert = stasjonRepository.insert(stasjonParams)
            require(insert is Either.Right)
            stasjon = insert.b
        }

        transaction {
            val insert = partnerRepository.insert(partnerParams)
            require(insert is Either.Right)
            partner = insert.b
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
            ekstraHenting1 = insert.b
        }

        val ekstraHentingCreateParams2 = object : EkstraHentingCreateParams() {
            override val stasjonId: UUID = stasjon.id
            override val startTidspunkt: LocalDateTime = LocalDateTime.now().plusHours(2)
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusHours(4)
            override val merknad: String = "EFGHIJK"
        }

        transaction {
            val insert = ekstraHentingRepository.insert(ekstraHentingCreateParams2)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.stasjonId == stasjon.id)
            ekstraHenting2 = insert.b
        }
    }

    @AfterEach
    fun tearDown() {
        testContainer.stop()
    }

    @Test
    fun insert() {

        val wrongIdParams = object : EkstraHentingCreateParams() {
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusHours(2)
            override val merknad: String? = null
            override val stasjonId: UUID = UUID.randomUUID()
        }

        transaction {
            val insert = ekstraHentingRepository.insert(wrongIdParams)
            println(insert)
            require(insert is Either.Left)
            assert(insert.a is RepositoryError.InsertError)
        }

        val correctIdParams = object : EkstraHentingCreateParams() {
            override val startTidspunkt: LocalDateTime = LocalDateTime.now()
            override val sluttTidspunkt: LocalDateTime = LocalDateTime.now().plusHours(2)
            override val merknad: String? = null
            override val stasjonId: UUID = stasjon.id
        }

        transaction {
            val insert = ekstraHentingRepository.insert(correctIdParams)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.stasjonId == stasjon.id)
        }

    }

    @Test
    fun update() {

        val updatedText = "I have been updated!"

        transaction {
            val findHenting = ekstraHentingRepository.findOne(ekstraHenting1.id)
            require(findHenting is Either.Right)
            assert(findHenting.b == ekstraHenting1)
            println(findHenting.b.merknad)
        }

        transaction {
            val kategorier = listOf<EkstraHentingKategoriBatchSaveDto>(EkstraHentingKategoriBatchSaveDto(kategoriId = UUID.fromString("cc4912ef-e2ed-4460-9c50-39caffde79de"), mengde = 120f))
            val update = ekstraHentingRepository.update(EkstraHentingUpdateDto(id=ekstraHenting1.id, merknad = updatedText, kategorier = kategorier))
            require(update is Either.Right)
            assertEquals(updatedText, update.b.merknad)
        }

        transaction {
            val findHenting = ekstraHentingRepository.findOne(ekstraHenting1.id)
            require(findHenting is Either.Right)
            assert(findHenting.b.merknad == updatedText)
        }

    }

    @Test
    fun findOne() {
        val wrongId = UUID.randomUUID()
        transaction {
            val findOne = ekstraHentingRepository.findOne(wrongId)
            require(findOne is Either.Left)
            assert(findOne.a is RepositoryError.NoRowsFound)
        }

        transaction {
            val findOne = ekstraHentingRepository.findOne(ekstraHenting1.id)
            println(findOne)
            require(findOne is Either.Right)
            assert(findOne.b == ekstraHenting1)
        }
    }

    @Test
    fun delete() {
        transaction {
            val findEkstraHenting = ekstraHentingRepository.findOne(ekstraHenting1.id)
            require(findEkstraHenting is Either.Right)
            assert(findEkstraHenting.b == ekstraHenting1)
        }

        transaction {
            val deleteEkstraHenting = ekstraHentingRepository.delete(ekstraHenting1.id)
            assert(deleteEkstraHenting is Either.Right)
        }

        transaction {
            val findEkstraHenting = ekstraHentingRepository.findOne(ekstraHenting1.id)
            require(findEkstraHenting is Either.Left)
            assert(findEkstraHenting.a is RepositoryError.NoRowsFound)
        }

        transaction {
            val deleteEkstraHenting = ekstraHentingRepository.delete(ekstraHenting1.id)
            assert(deleteEkstraHenting is Either.Right)
        }
    }

    @Test
    fun find() {
        transaction {
            val findAll = ekstraHentingRepository.find(EkstraHentingFindDto())
            println(findAll)
            require(findAll is Either.Right)
            assert(findAll.b.size == 2)
            assert(findAll.b.contains(ekstraHenting1))
        }

        transaction {
            val findWrongHenteplanId = ekstraHentingRepository.find(EkstraHentingFindDto(stasjonId = UUID.randomUUID()))
            require(findWrongHenteplanId is Either.Right)
            assert(findWrongHenteplanId.b.isEmpty())
        }

        transaction {
            val findCorrectHentepanId = ekstraHentingRepository.find(EkstraHentingFindDto(stasjonId = stasjon.id))
            println(findCorrectHentepanId)
            require(findCorrectHentepanId is Either.Right)
            assert(findCorrectHentepanId.b.size == 2)
            assert(findCorrectHentepanId.b[0] == ekstraHenting1)
        }


        //Searching by merknad not yet supported: should it be?
/*        transaction {
            val findMerknadABC = ekstraHentingRepository.find(EkstraHentingFindDto(merknad = "ABC"))
            println(findMerknadABC)
            require(findMerknadABC is Either.Right)
            assert(findMerknadABC.b.size == 1)
            assert(findMerknadABC.b.contains(ekstraHenting1))
        }

        transaction {
            val findMerknadEFG = ekstraHentingRepository.find(EkstraHentingFindDto(merknad = "EFG"))
            println(findMerknadEFG)
            require(findMerknadEFG is Either.Right)
            assert(findMerknadEFG.b.size == 2)
            assert(findMerknadEFG.b.containsAll(listOf(ekstraHenting1, ekstraHenting2)))
        }

        transaction {
            val findMerknadNotExisting = ekstraHentingRepository.find(EkstraHentingFindDto(merknad = "This merknad does not exist"))
            println(findMerknadNotExisting)
            require(findMerknadNotExisting is Either.Right)
            assert(findMerknadNotExisting.b.isEmpty())
        }*/

        transaction {
            val findAllBetween = ekstraHentingRepository.find(
                EkstraHentingFindDto(
                after = LocalDateTime.now().minusHours(1),
                before = LocalDateTime.now().plusHours(3)
            )
            )
            println(findAllBetween)
            require(findAllBetween is Either.Right)
            assert(findAllBetween.b.size == 1)
            assert(findAllBetween.b.contains(ekstraHenting1))
        }

        transaction {
            val findAllBetween = ekstraHentingRepository.find(
                EkstraHentingFindDto(
                after = LocalDateTime.now().minusHours(1),
                before = LocalDateTime.now().plusHours(5)
            )
            )
            println(findAllBetween)
            require(findAllBetween is Either.Right)
            assert(findAllBetween.b.size == 2)
            assert(findAllBetween.b.containsAll(listOf(ekstraHenting1, ekstraHenting2)))
        }

        transaction {
            val findAllBetween = ekstraHentingRepository.find(
                EkstraHentingFindDto(
                after = LocalDateTime.now().plusHours(1),
                before = LocalDateTime.now().plusHours(5)
            )
            )
            println(findAllBetween)
            require(findAllBetween is Either.Right)
            assert(findAllBetween.b.size == 1)
            assert(findAllBetween.b.contains(ekstraHenting2))
        }

        transaction {
            val findAllBefore = ekstraHentingRepository.find(
                EkstraHentingFindDto(
                before = LocalDateTime.now().plusHours(1)
            )
            )
            println(findAllBefore)
            require(findAllBefore is Either.Right)
            assert(findAllBefore.b.isEmpty())
        }
    }
}