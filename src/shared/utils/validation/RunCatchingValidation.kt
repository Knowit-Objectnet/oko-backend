package ombruk.backend.shared.utils.validation

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.shared.error.ValidationError
import org.valiktor.ConstraintViolationException
import org.valiktor.i18n.mapToMessage
import java.util.*

/**
 * Supply a valiktor validation function and and get the validated or a Validation error form the valiktor function.
 */
fun <T> runCatchingValidation(func: () -> T): Either<ValidationError, T> {
    return try {
        func().right()
    } catch (e: ConstraintViolationException) {
        val msg = e.constraintViolations.mapToMessage("messages", Locale.ENGLISH)
            .joinToString { "${it.property}: ${it.message}" }
        ValidationError.Unprocessable(msg).left()
    }
}