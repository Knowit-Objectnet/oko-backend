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
import ombruk.backend.calendar.model.Event
import ombruk.backend.reporting.model.Report
import ombruk.backend.shared.error.AuthorizationError
import ombruk.backend.shared.error.ServiceError

enum class Roles(val value: String) {
    Partner("partner"),
    RegEmployee("reg_employee"),
    ReuseStation("reuse_station")
}

object Authorization {

    var appConfig = HoconApplicationConfig(ConfigFactory.load())
    private val debug: Boolean = appConfig.property("ktor.oko.debug").getString().toBoolean()
    var testing: Boolean = false

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

    fun authorizePartnerID(role: Pair<Roles, Int>, eventsFunc: () -> Either<ServiceError, List<Event>>) = eventsFunc()
        .flatMap {
            if (role.first == Roles.Partner && it.any { event -> event.partner.id != role.second }) {
                AuthorizationError.AccessViolationError().left()
            } else role.right()
        }

    fun authorizeReportPatchByPartnerId(role: Pair<Roles, Int>, reportFunc: () -> Either<ServiceError, Report>) =
        reportFunc()
            .flatMap {
                if (role.first == Roles.Partner && it.partnerID != role.second) {
                    AuthorizationError.AccessViolationError().left()
                } else role.right()
            }

    fun authorizeRequestId(role: Pair<Roles, Int>, partnerId: Int): Either<AuthorizationError, Pair<Roles, Int>> {
        println("hello")
        return when (partnerId == role.second) {
            true -> role.right()
            else -> AuthorizationError.AccessViolationError("Provided ID $partnerId does not match your ID ${role.second}")
                .left()
        }
    }

    fun authorizePickupId(role: Pair<Roles, Int>, partnerId: Int) = if (role.first == Roles.Partner) {
        when (role.second == partnerId) {
            true -> role.right()
            else -> AuthorizationError.AccessViolationError("Provided id $partnerId does not match your ID ${role.second}")
                .left()
        }
    } else {
        role.right()
    }
}