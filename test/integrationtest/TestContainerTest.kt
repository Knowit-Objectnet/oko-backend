import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers
import testutils.TestContainer
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class TestContainerTest {
    private val testContainer: TestContainer = TestContainer()

    @BeforeAll
    fun setup() {
        testContainer.start()
    }

    @AfterAll
    fun tearDown() {
        testContainer.stop()
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