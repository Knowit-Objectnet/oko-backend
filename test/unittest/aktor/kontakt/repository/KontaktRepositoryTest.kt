package aktor.kontakt.repository

import arrow.core.Either
import arrow.core.right
import arrow.core.rightIfNotNull
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.model.KontaktCreateParams
import ombruk.backend.aktor.domain.model.KontaktFindParams
import ombruk.backend.aktor.domain.model.KontaktUpdateParams
import ombruk.backend.aktor.infrastructure.repository.KontaktRepository
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class KontaktRepositoryTest {
    private val testContainer: TestContainer = TestContainer()
    private lateinit var kontaktRepository: KontaktRepository

    @BeforeEach
    fun setup() {
        testContainer.start()
        kontaktRepository = KontaktRepository()
    }

    @AfterEach
    fun tearDown() {
        testContainer.stop()
    }

    @Test
    fun testInsert() {
        val aktorId = UUID.randomUUID()
        val kontakt = object: KontaktCreateParams() {
            override val aktorId: UUID = aktorId
            override val navn: String = "Oslo"
            override val telefon: String = "40404040"
            override val epost: String = "leder@oslo.no"
            override val rolle: String = "Leder"
        }

        transaction {
            val insert = kontaktRepository.insert(kontakt)
            assert(insert is Either.Right)
            val k = insert.fold({ null }, {it})
            if (k != null) {
                assertEquals(k.aktorId, aktorId)
            }
        }
    }

    @Test
    fun testFindOne() {
        val id = UUID.randomUUID()
        val findOne = transaction { kontaktRepository.findOne(id) }
        require(findOne is Either.Left)
        assert(findOne.a is RepositoryError.NoRowsFound)

    }

    @Test
    fun testFind() {
        val findParams = object : KontaktFindParams() {
            override val id: UUID? = null
            override val aktorId: UUID? = null
            override val navn: String? = null
            override val telefon: String? = null
            override val epost: String? = null
            override val rolle: String? = null
        }

        val find = transaction { kontaktRepository.find(findParams) }
        println(find)
        require(find is Either.Right)
        assert(find.b.isEmpty())
    }

    @Test
    fun testFindOneInserted() {
        val aktorId = UUID.randomUUID()
        val kontakt = object: KontaktCreateParams() {
            override val aktorId: UUID = aktorId
            override val navn: String = "Oslo"
            override val telefon: String = "40404040"
            override val epost: String = "leder@oslo.no"
            override val rolle: String = "Leder"
        }

        var kontaktObj: Kontakt? = null

        transaction {
            val insert = kontaktRepository.insert(kontakt)
            val k = insert.fold({ null }, {it})
            kontaktObj = k
        }

        kontaktObj?.let {
            val findOne = transaction { kontaktRepository.findOne(kontaktObj!!.id) }
            require(findOne is Either.Right)
            assertEquals(kontaktObj, findOne.b )
        }
        kontaktObj ?: assert(false)
    }

    @Test
    fun testUpdate() {
        val aktorId = UUID.randomUUID()
        val kontakt = object: KontaktCreateParams() {
            override val aktorId: UUID = aktorId
            override val navn: String = "Oslo"
            override val telefon: String = "40404040"
            override val epost: String = "leder@oslo.no"
            override val rolle: String = "Leder"
        }

        var kontaktObj: Kontakt? = null

        transaction {
            val insert = kontaktRepository.insert(kontakt)
            val k = insert.fold({ null }, {it})
            kontaktObj = k
        }

        kontaktObj?.let {
            val updateParam = object: KontaktUpdateParams() {
                override val id: UUID = kontaktObj!!.id
                override val aktorId: UUID = kontaktObj!!.aktorId
                override val navn: String = kontaktObj!!.navn
                override val telefon: String? = kontaktObj!!.telefon
                override val epost: String? = kontaktObj!!.epost
                override val rolle: String = "Sjåfør"
            }
            transaction {
                val update = kontaktRepository.update(updateParam)
                val u = update.fold({ null }, { it })


                u.let {
                    assertNotEquals(u, kontaktObj)
                    assertEquals(u!!.id, kontaktObj!!.id)
                    assertNotEquals(u.rolle, kontaktObj!!.rolle)
                }
                // If update is null
                u ?: assert(false)
            }
        }
        // If inserted kontakt is null
        kontaktObj ?: assert(false)
    }
}