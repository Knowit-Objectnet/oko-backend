package no.oslokommune.ombruk.partner

import arrow.core.Either
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.partner.form.PartnerPostForm
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.partner.service.PartnerService
import no.oslokommune.ombruk.shared.database.initDB
import org.junit.jupiter.api.*
import no.oslokommune.ombruk.testDelete
import no.oslokommune.ombruk.testGet
import no.oslokommune.ombruk.testPatch
import no.oslokommune.ombruk.testPost
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PartnerTest {

    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    lateinit var partnere: List<Partner>

    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        partnere = createTestPartnere()
    }

    @AfterEach
    fun teardown() {
        PartnerRepository.deleteAllPartnere()
    }

    private fun createTestPartnere() = (0..9).map {
        val p = PartnerService.savePartner(
            PartnerPostForm(
                "no.oslokommune.ombruk.partner.PartnerTest Partner$it",
                "Description",
                "1234567$it",
                "test$it@gmail.com"
            )
        )
        require(p is Either.Right)
        return@map p.b
    }

    @Nested
    inner class Get {
        @Test
        fun `get partner by id`() {
            testGet("/partnere/${partnere[3].id}") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), partnere[3]), response.content)
            }
        }

        @Test
        fun `get all partnere`() {
            testGet("/partnere") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer().list, partnere), response.content)
            }
        }

        @Test
        fun `get partner by name`() {
            testGet("/partnere?name=${partnere[6].name}") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer().list, listOf(partnere[6])), response.content)
            }
        }
    }

    @Nested
    inner class Post {

        @Test
        fun `create partner with name`() {
            val name = "MyPartner"

            val body =
                """{
                    "name": "$name"
                }"""

            testPost("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responsePartner = json.parse(Partner.serializer(), response.content!!)
                val insertedPartner = PartnerRepository.getPartnerByID(responsePartner.id)
                require(insertedPartner is Either.Right)
                assertEquals(responsePartner, insertedPartner.b)
                assertEquals(name, insertedPartner.b.name)
            }
        }

        @Test
        fun `create partner with description`() {
            val name = "MyPartner"
            val description = "This is a desc."
            val body =
                """{
                    "name": "$name",
                    "description": "$description"
                }"""

            testPost("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responsePartner = json.parse(Partner.serializer(), response.content!!)
                val insertedPartner = PartnerRepository.getPartnerByID(responsePartner.id)
                require(insertedPartner is Either.Right)
                assertEquals(responsePartner, insertedPartner.b)
                assertEquals(name, insertedPartner.b.name)
                assertEquals(description, insertedPartner.b.description)
            }
        }

        @Test
        fun `create partner with phone`() {
            val name = "MyPartner"
            val phone = "+4712345678"
            val body =
                """{
                    "name": "$name",
                    "phone": "$phone"
                }"""

            testPost("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responsePartner = json.parse(Partner.serializer(), response.content!!)
                val insertedPartner = PartnerRepository.getPartnerByID(responsePartner.id)
                require(insertedPartner is Either.Right)
                assertEquals(responsePartner, insertedPartner.b)
                assertEquals(name, insertedPartner.b.name)
                assertEquals(phone, insertedPartner.b.phone)
            }
        }

        @Test
        fun `create partner with email`() {
            val name = "MyPartner"
            val email = "test@gmail.com"
            val body =
                """{
                    "name": "$name",
                    "email": "$email"
                }"""

            testPost("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responsePartner = json.parse(Partner.serializer(), response.content!!)
                val insertedPartner = PartnerRepository.getPartnerByID(responsePartner.id)
                require(insertedPartner is Either.Right)
                assertEquals(responsePartner, insertedPartner.b)
                assertEquals(name, insertedPartner.b.name)
                assertEquals(email, insertedPartner.b.email)
            }
        }

        @Test
        fun `create partner with everything`() {
            val name = "MyPartner"
            val description = "This is a desc."
            val phone = "12345678"
            val email = "test@gmail.com"
            val body =
                """{
                    "name": "$name",
                    "description": "$description",
                    "phone": "$phone",
                    "email": "$email"
                }"""

            testPost("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responsePartner = json.parse(Partner.serializer(), response.content!!)
                val insertedPartner = PartnerRepository.getPartnerByID(responsePartner.id)
                require(insertedPartner is Either.Right)
                assertEquals(responsePartner, insertedPartner.b)
                assertEquals(name, insertedPartner.b.name)
                assertEquals(email, insertedPartner.b.email)
                assertEquals(phone, insertedPartner.b.phone)
                assertEquals(description, insertedPartner.b.description)
            }
        }

    }

    @Nested
    inner class Patch {
        @Test
        fun `update partner description`() {
            val partnerToUpdate = partnere[9]
            val expectedResponse = partnerToUpdate.copy(description = "testing")
            val body =
                """{
                    "id": "${partnerToUpdate.id}",
                    "description": "testing"
                }"""

            testPatch("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expectedResponse), response.content)

                val partnerInRepository = PartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse, partnerInRepository.b)

            }
        }

        @Test
        fun `update partner email`() {
            val partnerToUpdate = partnere[1]
            val expectedResponse = partnerToUpdate.copy(email = "test@gmail.com")
            val body =
                """{
                    "id": "${partnerToUpdate.id}",
                    "email": "test@gmail.com"
                }"""

            testPatch("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expectedResponse), response.content)

                val partnerInRepository = PartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse, partnerInRepository.b)

            }
        }

        @Test
        fun `update partner phone`() {
            val partnerToUpdate = partnere[1]
            val expectedResponse = partnerToUpdate.copy(phone = "54612876")
            val body =
                """{
                    "id": "${partnerToUpdate.id}",
                    "phone": "54612876"
                }"""

            testPatch("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expectedResponse), response.content)

                val partnerInRepository = PartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse, partnerInRepository.b)

            }
        }

        @Test
        fun `update partner everything`() {
            val partnerToUpdate = partnere[1]
            val expectedResponse =
                partnerToUpdate.copy(email = "test@gmail.com", phone = "12345678", description = "testing")
            val body =
                """{
                    "id": "${partnerToUpdate.id}",
                    "email": "test@gmail.com",
                    "phone": "12345678",
                    "description": "testing"
                }"""

            testPatch("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expectedResponse), response.content)

                val partnerInRepository = PartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse, partnerInRepository.b)

            }
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `delete partner by id`() {
            testDelete("/partnere/${partnere[3].id}") {
                val respondedUttak = json.parse(Partner.serializer(), response.content!!)
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(partnere[3], respondedUttak)
                assertFalse(UttakRepository.exists(partnere[3].id))
            }
        }
    }
}