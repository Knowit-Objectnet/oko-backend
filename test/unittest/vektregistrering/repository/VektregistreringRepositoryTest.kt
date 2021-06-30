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
import ombruk.backend.vektregistrering.application.api.dto.VektregistreringFindDto
import ombruk.backend.vektregistrering.domain.entity.Vektregistrering
import ombruk.backend.vektregistrering.domain.params.VektregistreringCreateParams
import ombruk.backend.vektregistrering.infrastructure.repository.VektregistreringRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testutils.TestContainer
import java.time.LocalDateTime
import java.util.*

internal class VektregistreringRepositoryTest {

    private val testContainer: TestContainer = TestContainer()
    private lateinit var vektregistreringRepository: VektregistreringRepository
    private lateinit var ekstraHentingRepository: EkstraHentingRepository
    private lateinit var stasjonRepository: StasjonRepository
    private lateinit var partnerRepository: PartnerRepository
    private lateinit var stasjon: Stasjon
    private lateinit var partner1: Partner
    private lateinit var partner2: Partner
    private lateinit var ekstraHenting: EkstraHenting
    private lateinit var vektregistrering1: Vektregistrering
    private lateinit var vektregistrering2: Vektregistrering

    @BeforeEach
    fun setUp() {

        testContainer.start()
        ekstraHentingRepository = EkstraHentingRepository()
        stasjonRepository = StasjonRepository()
        partnerRepository = PartnerRepository()
        vektregistreringRepository = VektregistreringRepository()

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

        val vektregistreringCreateParams1 = object : VektregistreringCreateParams() {
            override val hentingId: UUID = ekstraHenting.id
            override val kategoriId: UUID = UUID.fromString("33812d39-75e9-4ba9-875f-b0724aa68185") //Kategori: Bøker
            override val vekt: Float = 100f
        }

        val vektregistreringCreateParams2 = object : VektregistreringCreateParams() {
            override val hentingId: UUID = ekstraHenting.id
            override val kategoriId: UUID = UUID.fromString("b1c6de98-4a11-4526-8298-f16886e5b552") //Kategori: Sykler
            override val vekt: Float = 300f
        }


        transaction {
            val insert = vektregistreringRepository.insert(vektregistreringCreateParams1)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.kategoriId == UUID.fromString("33812d39-75e9-4ba9-875f-b0724aa68185"))
            vektregistrering1 = insert.b
        }

        transaction {
            val insert = vektregistreringRepository.insert(vektregistreringCreateParams2)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.vekt == 300f)
            vektregistrering2 = insert.b
        }

    }

    @AfterEach
    fun tearDown() {
        testContainer.stop()
    }

    @Test
    fun insert() {

        val wrongIdParams = object : VektregistreringCreateParams() {
            override val hentingId: UUID = UUID.randomUUID()
            override val kategoriId: UUID = UUID.randomUUID()
            override val vekt: Float = 100f
        }

        transaction {
            val insert = vektregistreringRepository.insert(wrongIdParams)
            println(insert)
            require(insert is Either.Left)
            assert(insert.a is RepositoryError.InsertError)
        }

        val correctIdParams = object : VektregistreringCreateParams() {
            override val hentingId: UUID = ekstraHenting.id
            override val kategoriId: UUID = UUID.fromString("b1c6de98-4a11-4526-8298-f16886e5b552") //Kategori: Sykler
            override val vekt: Float = 300f
        }

        transaction {
            val insert = vektregistreringRepository.insert(correctIdParams)
            println(insert)
            require(insert is Either.Right)
            assert(insert.b.hentingId == ekstraHenting.id)
        }

    }


    @Test
    fun findOne() {
        val wrongId = UUID.randomUUID()
        transaction {
            val findOne = vektregistreringRepository.findOne(wrongId)
            require(findOne is Either.Left)
            assert(findOne.a is RepositoryError.NoRowsFound)
        }

        transaction {
            val findOne = vektregistreringRepository.findOne(vektregistrering1.id)
            println(findOne)
            require(findOne is Either.Right)
            assert(findOne.b == vektregistrering1)
        }
    }


    @Test
    fun delete() {

        transaction {
            val findVektregistrering = vektregistreringRepository.findOne(vektregistrering1.id)
            require(findVektregistrering is Either.Right)
            assert(findVektregistrering.b == vektregistrering1)
        }

        transaction {
            val deleteVektregistrering = vektregistreringRepository.delete(vektregistrering1.id)
            assert(deleteVektregistrering is Either.Right)
        }

        transaction {
            val findVektregistrering = vektregistreringRepository.findOne(vektregistrering1.id)
            require(findVektregistrering is Either.Left)
            assert(findVektregistrering.a is RepositoryError.NoRowsFound)
        }

        transaction {
            val deleteVektregistrering = vektregistreringRepository.delete(vektregistrering1.id)
            assert(deleteVektregistrering is Either.Right)
        }

    }

    @Test
    fun find() {

        transaction {
            val findAll = vektregistreringRepository.find(VektregistreringFindDto())
            println(findAll)
            require(findAll is Either.Right)
            assert(findAll.b.size == 2)
            assert(findAll.b[0] == vektregistrering1)
        }

        transaction {
            val findWrongHentingId = vektregistreringRepository.find(VektregistreringFindDto(hentingId = UUID.randomUUID()))
            require(findWrongHentingId is Either.Right)
            assert(findWrongHentingId.b.isEmpty())
        }

        transaction {
            val findCorrectHentingId = vektregistreringRepository.find(VektregistreringFindDto(hentingId = ekstraHenting.id))
            println(findCorrectHentingId)
            require(findCorrectHentingId is Either.Right)
            assert(findCorrectHentingId.b.size == 2)
            assert(findCorrectHentingId.b[0] == vektregistrering1)
        }

        transaction {
            val findCorrectPartnerId = vektregistreringRepository.find(VektregistreringFindDto(kategoriId = UUID.fromString("33812d39-75e9-4ba9-875f-b0724aa68185")))
            println(findCorrectPartnerId)
            require(findCorrectPartnerId is Either.Right)
            assert(findCorrectPartnerId.b.size == 1)
            assert(findCorrectPartnerId.b[0] == vektregistrering1)
        }

    }
}