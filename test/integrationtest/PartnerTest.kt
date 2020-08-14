
import arrow.core.Either
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.partner.form.PartnerPostForm
import ombruk.backend.partner.model.Partner
import ombruk.backend.partner.service.PartnerService
import ombruk.backend.shared.database.initDB
import org.junit.jupiter.api.*
import testutils.testDelete
import testutils.testGet
import testutils.testPatch
import testutils.testPost
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PartnerTest {

    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    lateinit var partners: List<Partner>

    init {
        initDB()
    }

    @BeforeEach
    fun setup() {
        partners = createTestPartners()
    }

    @AfterEach
    fun teardown() {
        PartnerRepository.deleteAllPartners()
    }

    private fun createTestPartners() = (0..9).map {
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
            testGet("/partners/${partners[3].id}") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), partners[3]), response.content)
            }
        }

        @Test
        fun `get all partners`() {
            testGet("/partners") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer().list, partners), response.content)
            }
        }

        @Test
        fun `get partner by name`() {
            testGet("/partners?name=${partners[6].name}") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer().list, listOf(partners[6])), response.content)
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

            testPost("/partners", body) {
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

            testPost("/partners", body) {
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

            testPost("/partners", body) {
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

            testPost("/partners", body) {
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

            testPost("/partners", body) {
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
            val partnerToUpdate = partners[9]
            val expectedResponse = partnerToUpdate.copy(description = "testing")
            val body =
                """{
                    "id": "${partnerToUpdate.id}",
                    "description": "testing"
                }"""

            testPatch("/partners", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expectedResponse), response.content)

                val partnerInRepository = PartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse, partnerInRepository.b)

            }
        }

        @Test
        fun `update partner email`() {
            val partnerToUpdate = partners[1]
            val expectedResponse = partnerToUpdate.copy(email = "test@gmail.com")
            val body =
                """{
                    "id": "${partnerToUpdate.id}",
                    "email": "test@gmail.com"
                }"""

            testPatch("/partners", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expectedResponse), response.content)

                val partnerInRepository = PartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse, partnerInRepository.b)

            }
        }

        @Test
        fun `update partner phone`() {
            val partnerToUpdate = partners[1]
            val expectedResponse = partnerToUpdate.copy(phone = "54612876")
            val body =
                """{
                    "id": "${partnerToUpdate.id}",
                    "phone": "54612876"
                }"""

            testPatch("/partners", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Partner.serializer(), expectedResponse), response.content)

                val partnerInRepository = PartnerRepository.getPartnerByID(expectedResponse.id)
                require(partnerInRepository is Either.Right)
                assertEquals(expectedResponse, partnerInRepository.b)

            }
        }

        @Test
        fun `update partner everything`() {
            val partnerToUpdate = partners[1]
            val expectedResponse = partnerToUpdate.copy(email = "test@gmail.com", phone = "12345678", description = "testing")
            val body =
                """{
                    "id": "${partnerToUpdate.id}",
                    "email": "test@gmail.com",
                    "phone": "12345678",
                    "description": "testing"
                }"""

            testPatch("/partners", body) {
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
            testDelete("/partners/${partners[3].id}") {
                val respondedEvents = json.parse(Partner.serializer(), response.content!!)
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(partners[3], respondedEvents)
                assertFalse(EventRepository.exists(partners[3].id))
            }
        }
    }
}