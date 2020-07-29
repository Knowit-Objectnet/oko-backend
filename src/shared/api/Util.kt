package ombruk.backend.shared.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonDecodingException
import ombruk.backend.partner.service.KeycloakIntegrationError
import ombruk.backend.shared.error.*
import org.slf4j.LoggerFactory
import java.time.format.DateTimeParseException

private val logger = LoggerFactory.getLogger("ombruk.backend.shared.api.util")

fun <T> catchingCall(left: ServiceError, func: () -> T) = runCatching { func() }
    .onFailure { logger.warn(it.message) }
    .fold({ it.right() }, { left.left() })

fun <T> receiveCatching(func: suspend () -> T) = runCatching { runBlocking { func() } }
    .fold({ it.right() }, {
        when (it) {
            is JsonDecodingException -> RequestError.MangledRequestBody(it.message!!).left()
            is DateTimeParseException -> RequestError.MangledRequestBody(it.message!!).left()
            else -> ServiceError().left()
        }
    })

fun generateResponse(result: Either<ServiceError, Any>) = when (result) {
    is Either.Left -> when (result.a) {
        is ValidationError, is RequestError -> Pair(HttpStatusCode.BadRequest, result.a.message)

        is AuthorizationError.MissingRolesError,
        is AuthorizationError.InvalidPrincipal -> Pair(HttpStatusCode.Unauthorized, result.a.message)

        is AuthorizationError -> Pair(HttpStatusCode.Forbidden, result.a.message)

        is RepositoryError.NoRowsFound,
        is KeycloakIntegrationError.NotFoundError -> Pair(HttpStatusCode.NotFound, result.a.message)
        is KeycloakIntegrationError.ConflictError -> Pair(HttpStatusCode.Conflict, result.a.message)
        is KeycloakIntegrationError.AuthenticationError -> Pair(HttpStatusCode.InternalServerError, result.a.message)
        else -> Pair(HttpStatusCode.InternalServerError, result.a.message)
    }
    is Either.Right -> Pair(HttpStatusCode.OK, result.b)
}
