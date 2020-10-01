package no.oslokommune.ombruk.shared.validation

import io.mockk.every
import io.mockk.mockk
import no.oslokommune.ombruk.shared.database.IRepositoryUniqueName
import no.oslokommune.ombruk.shared.utils.validation.isUniqueInRepository
import org.junit.jupiter.api.Test
import org.valiktor.ConstraintViolationException
import org.valiktor.validate
import kotlin.test.assertFailsWith

class UniqueInRepositoryTest {

    private val repository = mockk<IRepositoryUniqueName>()

    data class UniqueTest(val name: String?)

    /*
    if null, it won't be inserted. Thus, it should be valid
     */
    @Test
    fun `unique null is valid`() {
        val test = UniqueTest(null)
        validate(test) {
            validate(UniqueTest::name).isUniqueInRepository(repository)
        }
    }

    /*
    Should not throw exception if it is unique.
     */
    @Test
    fun `unique in repository`() {
        val test = UniqueTest("test")

        every { repository.exists("test") } returns false
        validate(test) {
            validate(UniqueTest::name).isUniqueInRepository(repository)
        }
    }

    /*
    Should throw exception if not unique
     */
    @Test
    fun `not unique in repository`() {
        val test = UniqueTest("test")

        every { repository.exists("test") } returns true
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(UniqueTest::name).isUniqueInRepository(repository)
            }
        }
    }
}