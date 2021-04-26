package ombruk.backend.shared.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import ombruk.backend.shared.error.*
import org.slf4j.LoggerFactory
import java.time.format.DateTimeParseException

private val logger = LoggerFactory.getLogger("ombruk.backend.shared.api.util")

/**
 * Helper function for receiving objects. Catches any errors that might occur during receival.
 * @param func The function to run. Usually a receive<[T]>()
 * @return A [ServiceError] on failure and the parsed [T] on success.
 */
fun <T> receiveCatching(func: suspend () -> T) = runCatching { runBlocking { func() } }
    .fold({ it.right() }, {
        when (it) {
//            is JsonDecodingException,
            is DateTimeParseException -> ValidationError.InputError(it.message!!).left()
            else -> {
                logger.error(it.message)
                ServiceError().left()
            }
        }
    })

/**
 * Helper function for generating a response. Even though this function can become quite beefy, it reduces
 * code reuse and ensures that all errors are handled as intended. The outputs of this result is intended to be used
 * when responding to a call, and must be destructured before being used.
 * @param result Either a [ServiceError] or [Any].
 * @return a [Pair] of a [HttpStatusCode] and [Any] to be used when responding to calls.
 */
fun generateResponse(result: Either<ServiceError, Any>) = when (result) {
    is Either.Left -> when (result.a) {
        is AuthorizationError.MissingRolesError,
        is AuthorizationError.MissingGroupIDError,
        is AuthorizationError.InvalidPrincipal -> Pair(HttpStatusCode.Unauthorized, result.a.message)
        is AuthorizationError -> Pair(HttpStatusCode.Forbidden, result.a.message)

        is ValidationError.InputError -> Pair(HttpStatusCode.BadRequest, result.a.message)
        is ValidationError.Unprocessable -> Pair(HttpStatusCode.UnprocessableEntity, result.a.message)

        is RepositoryError.NoRowsFound,
        is KeycloakIntegrationError.NotFoundError -> Pair(HttpStatusCode.NotFound, result.a.message)
        is KeycloakIntegrationError.ConflictError -> Pair(HttpStatusCode.Conflict, result.a.message)
        is KeycloakIntegrationError.AuthenticationError -> Pair(HttpStatusCode.InternalServerError, result.a.message)
        else -> Pair(HttpStatusCode.InternalServerError, result.a.message)
    }
    is Either.Right -> Pair(HttpStatusCode.OK, result.b)
}
