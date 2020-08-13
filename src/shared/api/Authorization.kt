package ombruk.backend.shared.api

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.typesafe.config.ConfigFactory
import io.ktor.application.ApplicationCall
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import io.ktor.config.HoconApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import ombruk.backend.calendar.model.Event
import ombruk.backend.reporting.model.Report
import ombruk.backend.shared.error.AuthorizationError
import ombruk.backend.shared.error.ServiceError

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
    fun authorizeRole(allowedRoles: List<Roles>, call: ApplicationCall): Either<AuthorizationError, Pair<Roles, Int>> {
        val principal = runCatching { call.principal<JWTPrincipal>()!! }.onFailure {
            return AuthorizationError.InvalidPrincipal().left()
        }.getOrElse { return AuthorizationError.InvalidPrincipal().left() }
        val claimRoles = when (debug || testing) {
            true -> {
                principal.payload.claims["roles"]?.asList(String::class.java)
            } //Have to do this because our JWT library doesn't support
            //objects within a claim
            else -> principal.payload.claims["realm_access"]?.asMap()?.get("roles") as List<String>?
        } ?: return AuthorizationError.MissingRolesError().left()
        val role = allowedRoles.firstOrNull { role -> println(role); claimRoles.any { it == role.value } }
            ?: return AuthorizationError.InsufficientRoleError().left()
        val groupID =
            principal.payload.claims["GroupID"]?.asInt() //-1 serves as a placeholder value for stations and REG admin
                ?: if (role != Roles.Partner) -1 else return AuthorizationError.MissingGroupIDError().left()
        return Pair(role, groupID).right()
    }

    /**
     * Checks if a partner is only attempting to access events that belong to them. If they do not own all the resources,
     * they should not be allowed to perform the operation.
     *
     * @param role A [Pair] of a [Roles] and an [Int] specifying what group they belong to.
     * @param eventsFunc A function that either returns a [ServiceError] or a [List] of [Event] objects.
     * @return [role] if the only [Event.partner.id] present in the results of [eventsFunc] is the one specified in [role].
     * Else, return a [AuthorizationError]
     */
    fun authorizePartnerID(role: Pair<Roles, Int>, eventsFunc: () -> Either<ServiceError, List<Event>>) = eventsFunc()
        .flatMap {
            if (role.first == Roles.Partner && it.any { event -> event.partner.id != role.second }) {
                AuthorizationError.AccessViolationError().left()
            } else role.right()
        }

    /**
     * Ensures that the ID in [role] corresponds to the [Report.partnerId] of the [Report] that is being updated.
     *
     * @param role A [Pair] of a [Roles] instance and an [Int] specifying what group they belong to.
     * @param reportFunc A function that returns either a [ServiceError] or the [Report] that should be updated.
     * @return [role] if the ID in [role] corresponds to the [Report.partnerId] from the [reportFunc]. Else, return [AuthorizationError]
     */
    fun authorizeReportPatchByPartnerId(role: Pair<Roles, Int>, reportFunc: () -> Either<ServiceError, Report>) =
        reportFunc()
            .flatMap {
                if (role.first == Roles.Partner && it.partnerId != role.second) {
                    AuthorizationError.AccessViolationError().left()
                } else role.right()
            }

    /**
     * Ensures that the ID in [role] corresponds to the [partnerId] passed in.
     * @param role A [Pair] of a [Roles] instance and an [Int] specifying what group they belong to.
     * @param partnerId The partner ID of the Request to be posted.
     * @return [role] if the two ID's match or an [AuthorizationError] if they differ.
     */
    fun authorizeRequestId(role: Pair<Roles, Int>, partnerId: Int) = when (partnerId == role.second) {
        true -> role.right()
        else -> AuthorizationError.AccessViolationError("Provided ID $partnerId does not match your ID ${role.second}")
            .left()
    }
}