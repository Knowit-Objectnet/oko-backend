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
            testGet("/partnere?navn=${partnere[6].navn}") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer().list, listOf(partnere[6])), response.content)
            }
        }
    }

    @Nested
    inner class Post {

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
                val insertedPartner = PartnerRepository.getPartnerByID(responsePartner.id)
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
        fun `update partner everything`() {
            val updatedNavn = "Donald Duck 123"
            val updatedBeskrivelse = "vel vel vel"
            val updatedTelefon = "81549300"
            val updatedEpost = "hestejente@gmail.com"

            val expectedResponse = partnere[1].copy(
                navn = updatedNavn,
                beskrivelse = updatedBeskrivelse,
                telefon = updatedTelefon,
                epost = updatedEpost
            )
            val body =
                """{
                    "id": "${expectedResponse.id}",
                    "navn": "$updatedNavn",
                    "beskrivelse": "$updatedBeskrivelse",
                    "telefon": "$updatedTelefon",
                    "epost": "$updatedEpost"
                }"""

            testPatch("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())

                val responsePartner = json.parse(Partner.serializer(), response.content!!)
                assertEquals(expectedResponse.id, responsePartner.id)
                assertEquals(expectedResponse.navn, responsePartner.navn)
                assertEquals(expectedResponse.beskrivelse, responsePartner.beskrivelse)
                assertEquals(expectedResponse.telefon, responsePartner.telefon)
                assertEquals(expectedResponse.epost, responsePartner.epost)
                assertEquals(expectedResponse.slettetTidspunkt, responsePartner.slettetTidspunkt)

                val partnerInRepository = PartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse.id, partnerInRepository.b.id)
                assertEquals(expectedResponse.navn, partnerInRepository.b.navn)
                assertEquals(expectedResponse.beskrivelse, partnerInRepository.b.beskrivelse)
                assertEquals(expectedResponse.telefon, partnerInRepository.b.telefon)
                assertEquals(expectedResponse.epost, partnerInRepository.b.epost)
                assertEquals(expectedResponse.slettetTidspunkt, partnerInRepository.b.slettetTidspunkt)

            }
        }

        @Test
        fun `update partner navn`() {
            val updatedNavn = "Donald Duck"

            val expectedResponse = partnere[1].copy(
                navn = updatedNavn
            )
            val body =
                """{
                    "id": "${expectedResponse.id}",
                    "navn": "$updatedNavn"
                }"""

            testPatch("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())

                val responsePartner = json.parse(Partner.serializer(), response.content!!)
                assertEquals(expectedResponse.id, responsePartner.id)
                assertEquals(expectedResponse.navn, responsePartner.navn)
                assertEquals(expectedResponse.beskrivelse, responsePartner.beskrivelse)
                assertEquals(expectedResponse.telefon, responsePartner.telefon)
                assertEquals(expectedResponse.epost, responsePartner.epost)
                assertEquals(expectedResponse.slettetTidspunkt, responsePartner.slettetTidspunkt)

                val partnerInRepository = PartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse.id, partnerInRepository.b.id)
                assertEquals(expectedResponse.navn, partnerInRepository.b.navn)
                assertEquals(expectedResponse.beskrivelse, partnerInRepository.b.beskrivelse)
                assertEquals(expectedResponse.telefon, partnerInRepository.b.telefon)
                assertEquals(expectedResponse.epost, partnerInRepository.b.epost)
                assertEquals(expectedResponse.slettetTidspunkt, partnerInRepository.b.slettetTidspunkt)

            }
        }

        @Test
        fun `update partner beskrivelse`() {
            val updatedBeskrivelse = "vel vel vel"

            val expectedResponse = partnere[1].copy(
                beskrivelse = updatedBeskrivelse
            )
            val body =
                """{
                    "id": "${expectedResponse.id}",
                    "beskrivelse": "$updatedBeskrivelse"
                }"""

            testPatch("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())

                val responsePartner = json.parse(Partner.serializer(), response.content!!)
                assertEquals(expectedResponse.id, responsePartner.id)
                assertEquals(expectedResponse.navn, responsePartner.navn)
                assertEquals(expectedResponse.beskrivelse, responsePartner.beskrivelse)
                assertEquals(expectedResponse.telefon, responsePartner.telefon)
                assertEquals(expectedResponse.epost, responsePartner.epost)
                assertEquals(expectedResponse.slettetTidspunkt, responsePartner.slettetTidspunkt)

                val partnerInRepository = PartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse.id, partnerInRepository.b.id)
                assertEquals(expectedResponse.navn, partnerInRepository.b.navn)
                assertEquals(expectedResponse.beskrivelse, partnerInRepository.b.beskrivelse)
                assertEquals(expectedResponse.telefon, partnerInRepository.b.telefon)
                assertEquals(expectedResponse.epost, partnerInRepository.b.epost)
                assertEquals(expectedResponse.slettetTidspunkt, partnerInRepository.b.slettetTidspunkt)

            }
        }

        @Test
        fun `update partner telefon`() {
            val updatedTelefon = "81549300"

            val expectedResponse = partnere[1].copy(
                telefon = updatedTelefon
            )
            val body =
                """{
                    "id": "${expectedResponse.id}",
                    "telefon": "$updatedTelefon"
                }"""

            testPatch("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())

                val responsePartner = json.parse(Partner.serializer(), response.content!!)
                assertEquals(expectedResponse.id, responsePartner.id)
                assertEquals(expectedResponse.navn, responsePartner.navn)
                assertEquals(expectedResponse.beskrivelse, responsePartner.beskrivelse)
                assertEquals(expectedResponse.telefon, responsePartner.telefon)
                assertEquals(expectedResponse.epost, responsePartner.epost)
                assertEquals(expectedResponse.slettetTidspunkt, responsePartner.slettetTidspunkt)

                val partnerInRepository = PartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse.id, partnerInRepository.b.id)
                assertEquals(expectedResponse.navn, partnerInRepository.b.navn)
                assertEquals(expectedResponse.beskrivelse, partnerInRepository.b.beskrivelse)
                assertEquals(expectedResponse.telefon, partnerInRepository.b.telefon)
                assertEquals(expectedResponse.epost, partnerInRepository.b.epost)
                assertEquals(expectedResponse.slettetTidspunkt, partnerInRepository.b.slettetTidspunkt)

            }
        }

        @Test
        fun `update partner epost`() {
            val updatedEpost = "hestejente@gmail.com"

            val expectedResponse = partnere[1].copy(
                epost = updatedEpost
            )
            val body =
                """{
                    "id": "${expectedResponse.id}",
                    "epost": "${updatedEpost}"
                }"""

            testPatch("/partnere", body) {
                assertEquals(HttpStatusCode.OK, response.status())

                val responsePartner = json.parse(Partner.serializer(), response.content!!)
                assertEquals(expectedResponse.id, responsePartner.id)
                assertEquals(expectedResponse.navn, responsePartner.navn)
                assertEquals(expectedResponse.beskrivelse, responsePartner.beskrivelse)
                assertEquals(expectedResponse.telefon, responsePartner.telefon)
                assertEquals(expectedResponse.epost, responsePartner.epost)
                assertEquals(expectedResponse.slettetTidspunkt, responsePartner.slettetTidspunkt)

                val partnerInRepository = PartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse.id, partnerInRepository.b.id)
                assertEquals(expectedResponse.navn, partnerInRepository.b.navn)
                assertEquals(expectedResponse.beskrivelse, partnerInRepository.b.beskrivelse)
                assertEquals(expectedResponse.telefon, partnerInRepository.b.telefon)
                assertEquals(expectedResponse.epost, partnerInRepository.b.epost)
                assertEquals(expectedResponse.slettetTidspunkt, partnerInRepository.b.slettetTidspunkt)

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