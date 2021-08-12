package ombruk.backend.shared.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.typesafe.config.ConfigFactory
import io.ktor.application.ApplicationCall
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import io.ktor.config.HoconApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import ombruk.backend.shared.error.AuthorizationError

import java.util.*

/**
 * Roles that corresponds to the roles created in the keycloak realm. If roles are changed in keycloak,
 * they must be changed here as well.
 */
enum class Roles(val value: String) {
    Partner("partner"),
    RegEmployee("reg_employee"),
    ReuseStation("reuse_station")
}

/**
 * Authorization is currently rather bare-bones, due to us not wanting to couple to keycloak too tightly.
 */
object Authorization {

    @KtorExperimentalAPI
    var appConfig = HoconApplicationConfig(ConfigFactory.load())

    @KtorExperimentalAPI
    private val debug: Boolean = appConfig.property("ktor.oko.debug").getString().toBoolean()
    var testing: Boolean = false

    /**
     * Function for ensuring that a requesting user is allowed to access an operation with their current role.
     * @param allowedRoles A [List] of [Roles] that is allowed access to the operation in the context of the function call.
     * @param call A [ApplicationCall] that ensures that the function can receive a [JWTPrincipal] so that roles and GroupID can be extracted.
     * @return An [AuthorizationError] on failure and a [Pair] of a [Roles] and an [Int] on success.
     */
    fun authorizeRole(allowedRoles: List<Roles>, call: ApplicationCall): Either<AuthorizationError, Pair<Roles, UUID>> {
        val principal = runCatching { call.principal<JWTPrincipal>()!! }.onFailure {
            return AuthorizationError.InvalidPrincipal().left()
        }.getOrElse { return AuthorizationError.InvalidPrincipal().left() }

        val claimRoles = when (debug || testing) {
            //Have to do this because our JWT library doesn't support
            //objects within a claim
            true -> principal.payload.claims["roles"]?.asList(String::class.java)
            else -> principal.payload.claims["realm_access"]?.asMap()?.get("roles") as List<String>?
        } ?: return AuthorizationError.MissingRolesError().left()

        val role = allowedRoles.firstOrNull { role -> println(role); claimRoles.any { it == role.value } }
            ?: return AuthorizationError.InsufficientRoleError().left()

        val groupIDString =
            if (role == Roles.RegEmployee) "00000000-0000-0000-0000-000000000000"
                else principal.payload.claims["GroupID"]?.asString()
                ?: return AuthorizationError.MissingGroupIDError().left()

        val groupID = UUID.fromString(groupIDString)

        return Pair(role, groupID).right()
    }
}