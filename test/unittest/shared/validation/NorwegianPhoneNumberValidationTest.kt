package shared.validation

import ombruk.backend.shared.utils.validation.isNorwegianPhoneNumberOrBlank
import org.junit.jupiter.api.Test
import org.valiktor.ConstraintViolationException
import org.valiktor.validate
import kotlin.test.assertFailsWith

class NorwegianPhoneNumberValidationTest {

    data class PhoneTest(val number: String?)

    /*
    null should be valid
     */
    @Test
    fun `null is valid`() {
        val test = PhoneTest(null)
        validate(test) {
            validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
        }
    }

    /*
    A random non-conforming string with length 8 should be invalid
     */
    @Test
    fun `random string is invalid`() {
        val test = PhoneTest("eplekake")
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
            }
        }
    }

    /*
    A random non-conforming string with +47 and then length 8 should be invalid
     */
    @Test
    fun `random string with +47 first is invalid`() {
        val test = PhoneTest("+47eplekake")
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
            }
        }
    }

    /*
    A random non-conforming string with 0047 and then length 8 should be invalid
     */
    @Test
    fun `random string with 0047 first is invalid`() {
        val test = PhoneTest("0047eplekake")
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
            }
        }
    }

    /*
    A mix of numbers and characters should be invalid
     */
    @Test
    fun `mix of numbers and characters is invalid`() {
        val test = PhoneTest("e1l2k3k4")
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
            }
        }
    }

    /*
    +47d{8} should be valid
     */
    @Test
    fun `+47(4|9)d{7} is valid`() {
        val test = PhoneTest("+4798765432")
        validate(test) {
            validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
        }
    }


    /*
    +47d{9} should be invalid
     */
    @Test
    fun `+47d{9} is invalid`() {
        val test = PhoneTest("+47987654321")
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
            }
        }
    }

    /*
    +47d{7} should be invalid
     */
    @Test
    fun `+47d{7} is invalid`() {
        val test = PhoneTest("+479876543")
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
            }
        }
    }

    /*
    0047 d{8} should not be valid
     */
    @Test
    fun `0047d{8} is invalid`() {
        val test = PhoneTest("004712345678")
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
            }
        }
    }

    /*
    0047d{7} should be invalid
     */
    @Test
    fun `0047d{7} is invalid`() {
        val test = PhoneTest("00471234567")
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
            }
        }
    }

    /*
    0047d{9} should be invalid
     */
    @Test
    fun `0047d{9} is invalid`() {
        val test = PhoneTest("0047123456789")
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
            }
        }
    }

    /*
    d{8} should not be valid
     */
    @Test
    fun `d{8} is valid`() {
        val test = PhoneTest("98765432")
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
            }
        }
    }

    /*
    d{9} should be invalid
     */
    @Test
    fun `d{9} is invalid`() {
        val test = PhoneTest("987654321")
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
            }
        }
    }

    /*
    d{7} should be invalid
     */
    @Test
    fun `d{7} is invalid`() {
        val test = PhoneTest("9876543")
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
            }
        }
    }

    /*
    47d{8} should be invalid
     */
    @Test
    fun `47d{8} is valid`() {
        val test = PhoneTest("4798765432")
        assertFailsWith(ConstraintViolationException::class) {
            validate(test) {
                validate(PhoneTest::number).isNorwegianPhoneNumberOrBlank()
            }
        }
    }
}