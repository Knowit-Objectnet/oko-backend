package ombruk.backend.shared.api

import arrow.core.Either
import io.ktor.http.HttpStatusCode
import ombruk.backend.partner.service.KeycloakIntegrationError
import ombruk.backend.shared.error.*

fun generateResponse(result: Either<ServiceError, Any>) = when (result) {
    is Either.Left -> when (result.a) {
        is ValidationError, is RequestError -> Pair(HttpStatusCode.BadRequest, result.a.message)
        is AuthorizationError -> Pair(HttpStatusCode.Unauthorized, result.a.message)

        is RepositoryError.NoRowsFound,
        is KeycloakIntegrationError.NotFoundError -> Pair(HttpStatusCode.NotFound, result.a.message)
        is KeycloakIntegrationError.ConflictError -> Pair(HttpStatusCode.Conflict, result.a.message)
        is KeycloakIntegrationError.AuthenticationError -> Pair(HttpStatusCode.InternalServerError, result.a.message)
        else -> Pair(HttpStatusCode.InternalServerError, result.a.message)
    }
    is Either.Right -> Pair(HttpStatusCode.OK, result.b)
}
