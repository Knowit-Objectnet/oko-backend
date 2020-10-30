package no.oslokommune.ombruk.partner

import arrow.core.Either
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.partner.database.SamPartnerRepository
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
        SamPartnerRepository.deleteAllPartnere()
    }

    private fun createTestPartnere() = (0..9).map {
        val p = PartnerService.savePartner(
            PartnerPostForm(
                "PartnerTest Partner$it",
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
            testGet("/partnere?name=${partnere[6].navn}") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer().list, listOf(partnere[6])), response.content)
            }
        }
    }

    @Nested
    inner class Post {

        @Disabled
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
                val insertedPartner = SamPartnerRepository.getPartnerByID(responsePartner.id)
                require(insertedPartner is Either.Right)
                assertEquals(responsePartner, insertedPartner.b)
                assertEquals(name, insertedPartner.b.navn)
            }
        }

        @Disabled
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
                val insertedPartner = SamPartnerRepository.getPartnerByID(responsePartner.id)
                require(insertedPartner is Either.Right)
                assertEquals(responsePartner, insertedPartner.b)
                assertEquals(name, insertedPartner.b.navn)
                assertEquals(description, insertedPartner.b.beskrivelse)
            }
        }

        @Disabled
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
                val insertedPartner = SamPartnerRepository.getPartnerByID(responsePartner.id)
                require(insertedPartner is Either.Right)
                assertEquals(responsePartner, insertedPartner.b)
                assertEquals(name, insertedPartner.b.navn)
                assertEquals(phone, insertedPartner.b.telefon)
            }
        }

        @Disabled
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
                val insertedPartner = SamPartnerRepository.getPartnerByID(responsePartner.id)
                require(insertedPartner is Either.Right)
                assertEquals(responsePartner, insertedPartner.b)
                assertEquals(name, insertedPartner.b.navn)
                assertEquals(email, insertedPartner.b.epost)
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
                    "navn": "$name",
                    "beskrivelse": "$description",
                    "telefon": "$phone",
                    "epost": "$email"
                }"""

            testPost("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responsePartner = json.parse(Partner.serializer(), response.content!!)
                val insertedPartner = SamPartnerRepository.getPartnerByID(responsePartner.id)
                require(insertedPartner is Either.Right)
                assertEquals(responsePartner, insertedPartner.b)
                assertEquals(name, insertedPartner.b.navn)
                assertEquals(email, insertedPartner.b.epost)
                assertEquals(phone, insertedPartner.b.telefon)
                assertEquals(description, insertedPartner.b.beskrivelse)
            }
        }

    }

    @Nested
    inner class Patch {
        @Test
        fun `update partner description`() {
            val partnerToUpdate = partnere[9]
            val expectedResponse = partnerToUpdate.copy(beskrivelse = "testing")
            val body =
                """{
                    "id": "${partnerToUpdate.id}",
                    "beskrivelse": "testing"
                }"""

            testPatch("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expectedResponse), response.content)

                val partnerInRepository = SamPartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse, partnerInRepository.b)

            }
        }

        @Test
        fun `update partner email`() {
            val partnerToUpdate = partnere[1]
            val expectedResponse = partnerToUpdate.copy(epost = "test@gmail.com")
            val body =
                """{
                    "id": "${partnerToUpdate.id}",
                    "epost": "test@gmail.com"
                }"""

            testPatch("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expectedResponse), response.content)

                val partnerInRepository = SamPartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse, partnerInRepository.b)

            }
        }

        @Test
        fun `update partner phone`() {
            val partnerToUpdate = partnere[1]
            val expectedResponse = partnerToUpdate.copy(telefon = "54612876")
            val body =
                """{
                    "id": "${partnerToUpdate.id}",
                    "telefon": "54612876"
                }"""

            testPatch("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expectedResponse), response.content)

                val partnerInRepository = SamPartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse, partnerInRepository.b)

            }
        }

        @Test
        fun `update partner everything`() {
            val partnerToUpdate = partnere[1]
            val expectedResponse =
                partnerToUpdate.copy(epost = "test@gmail.com", telefon = "12345678", beskrivelse = "testing")
            val body =
                """{
                    "id": "${partnerToUpdate.id}",
                    "epost": "test@gmail.com",
                    "telefon": "12345678",
                    "beskrivelse": "testing"
                }"""

            testPatch("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expectedResponse), response.content)

                val partnerInRepository = SamPartnerRepository.getPartnerByID(expectedResponse.id)
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