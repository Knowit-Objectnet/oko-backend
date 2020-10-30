package no.oslokommune.ombruk.shared.api

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
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.model.Uttaksdata
import no.oslokommune.ombruk.shared.error.AuthorizationError
import no.oslokommune.ombruk.shared.error.ServiceError

/**
 * Roles that corresponds to the roles created in the keycloak realm. If roles are changed in keycloak,
 * they must be changed here as well.
 */
enum class Roles(val value: String) {
    Partner("partner"),
    RegEmployee("reg_employee"),
    ReuseStasjon("reuse_stasjon")
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
            //Have to do this because our JWT library doesn't support
            //objects within a claim
            true -> principal.payload.claims["roles"]?.asList(String::class.java)
            else -> principal.payload.claims["realm_access"]?.asMap()?.get("roles") as List<String>?
        } ?: return AuthorizationError.MissingRolesError().left()

        val role = allowedRoles.firstOrNull { role -> println(role); claimRoles.any { it == role.value } }
            ?: return AuthorizationError.InsufficientRoleError().left()

        val groupID =
            principal.payload.claims["GroupID"]?.asInt() //-1 serves as a placeholder value for stasjoner and REG admin
                ?: if (role != Roles.Partner) -1 else return AuthorizationError.MissingGroupIDError().left()

        return Pair(role, groupID).right()
    }

    /**
     * Checks if a partner is only attempting to access uttak that belong to them. If they do not own all the resources,
     * they should not be allowed to perform the operation.
     *
     * @param role A [Pair] of a [Roles] and an [Int] specifying what group they belong to.
     * @param uttaksFunc A function that either returns a [ServiceError] or a [List] of [Uttak] objects.
     * @return [role] if the only [Uttak.partner.id] present in the results of [uttaksFunc] is the one specified in [role].
     * Else, return a [AuthorizationError]
     */
    fun authorizePartnerID(role: Pair<Roles, Int>, uttaksFunc: () -> Either<ServiceError, List<Uttak>>) = uttaksFunc()
        .flatMap {
            if (role.first == Roles.Partner && it.any { uttak -> uttak.partner?.id != role.second }) {
                AuthorizationError.AccessViolationError().left()
            } else role.right()
        }

    /**
     * Ensures that the ID in [role] corresponds to the [Uttaksdata.partnerId] of the [Uttaksdata] that is being updated.
     *
     * @param role A [Pair] of a [Roles] instance and an [Int] specifying what group they belong to.
     * @param uttaksdataFunc A function that returns either a [ServiceError] or the [Uttaksdata] that should be updated.
     * @return [role] if the ID in [role] corresponds to the [Uttaksdata.partnerId] from the [uttaksdataFunc]. Else, return [AuthorizationError]
     */
    fun authorizeReportPatchByPartnerId(role: Pair<Roles, Int>, uttaksdataFunc: () -> Either<ServiceError, Uttak>) =
        uttaksdataFunc()
            .flatMap {
                /**
                 * If no samarbeidspartnerID, then this is an "ekstra-uttak" and should only
                 * be patchable by reg employees.
                 * Otherwise, check if the uttak belongs to the partner
                 */
                if (it.samarbeidspartnerID == null && role.first == Roles.RegEmployee || // TODO: Verify with customer
                        role.first == Roles.Partner && it.samarbeidspartnerID == role.second) {
                    role.right()
                } else AuthorizationError.AccessViolationError().left()
            }

    /**
     * Ensures that the ID in [role] corresponds to the [partnerId] passed in.
     * @param role A [Pair] of a [Roles] instance and an [Int] specifying what group they belong to.
     * @param partnerId The partner ID of the Uttaksforesporsel to be posted.
     * @return [role] if the two ID's match or an [AuthorizationError] if they differ.
     */
    fun authorizeRequestId(role: Pair<Roles, Int>, partnerId: Int) = when (partnerId == role.second) {
        true -> role.right()
        else -> AuthorizationError.AccessViolationError("Provided ID $partnerId does not match your ID ${role.second}")
            .left()
    }
}