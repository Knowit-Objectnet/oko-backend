package shared.api

import arrow.core.left
import arrow.core.right
import io.ktor.http.HttpStatusCode
import ombruk.backend.shared.api.generateResponse
import ombruk.backend.shared.error.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UtilTest {

    @Suppress("unused")
    fun parameterProvider() = listOf(
        AuthorizationError.MissingGroupIDError() to HttpStatusCode.Unauthorized,
        AuthorizationError.MissingRolesError() to HttpStatusCode.Unauthorized,
        AuthorizationError.InvalidPrincipal() to HttpStatusCode.Unauthorized,
        AuthorizationError.AccessViolationError() to HttpStatusCode.Forbidden,
        ValidationError.InputError("") to HttpStatusCode.BadRequest,
        ValidationError.Unprocessable("") to HttpStatusCode.UnprocessableEntity,
        RepositoryError.NoRowsFound("") to HttpStatusCode.NotFound,
        KeycloakIntegrationError.NotFoundError() to HttpStatusCode.NotFound,
        KeycloakIntegrationError.ConflictError() to HttpStatusCode.Conflict,
        KeycloakIntegrationError.AuthenticationError() to HttpStatusCode.InternalServerError,
        ServiceError("") to HttpStatusCode.InternalServerError
    )

    @ParameterizedTest
    @MethodSource("parameterProvider")
    fun  `generate response test error`(errorToCode: Pair<ServiceError, HttpStatusCode>) {
        val expected = errorToCode.second to errorToCode.first.message
        val actual = generateResponse(errorToCode.first.left())
        assertEquals(expected, actual)
    }

    @Test
    fun `generate response success`(){
        val expected = HttpStatusCode.OK to ""
        val actual = generateResponse("".right())
        assertEquals(expected, actual)
    }

}