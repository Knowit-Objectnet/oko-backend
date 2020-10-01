package no.oslokommune.ombruk.shared.validation

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import no.oslokommune.ombruk.shared.database.IRepository
import no.oslokommune.ombruk.shared.utils.validation.isInRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.valiktor.ConstraintViolationException
import org.valiktor.validate
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class ExistsInRepositoryTest {

    private val repository = mockk<IRepository>()

    data class ExistsTest(val id: Int?)

    /*
        if it exists, nothing should be thrown
     */
    @Test
    fun `does exist valid`() {
        every { repository.exists(1) } returns true

        val test = ExistsTest(1)
        validate(test) {
            validate(ExistsTest::id).isInRepository(repository)
        }
    }

    /*
    If it does not exist, throw exception
     */
    @Test
    fun `does exist in repository invalid`() {
        every { repository.exists(1) } returns false
        val test = ExistsTest(1)
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(ExistsTest::id).isInRepository(repository)
            }
        }
    }

    /*
    Should be valid if null
     */
    @Test
    fun `exists in repo null value`() {
        val test = ExistsTest(null)
        validate(test) {
            validate(ExistsTest::id).isInRepository(repository)
        }
    }
}