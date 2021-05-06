package aktor.kontakt.repository

import arrow.core.Either
import arrow.core.flatMap
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.model.KontaktCreateParams
import ombruk.backend.aktor.infrastructure.repository.KontaktRepository
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import javax.swing.text.html.parser.Entity
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class TestContainerTest {
    private lateinit var testContainer: TestContainer

    @BeforeEach
    fun setup() {
        testContainer = TestContainer()
    }

    @Test
    fun testNativeSQL() {
        // Create test table
        val query =
            """
                create TABLE test (
                    id serial primary key,
                    name varchar(255) not null unique
                )
            """.trimIndent()
        testContainer.exec(query) {}

        // Insert value to test table
        testContainer.exec("insert into test(name) values ('test')") {}

        // Get all values from test table
        val result = testContainer.exec("select * from test") { rs ->
            rs.getString("name")
        }

        assertTrue(result.count() == 1)
        assertEquals("test", result[0])
    }
}