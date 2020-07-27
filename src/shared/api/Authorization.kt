package ombruk.backend.shared.api

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.ktor.application.ApplicationCall
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import ombruk.backend.shared.error.AuthorizationError
import ombruk.backend.calendar.model.Event
import ombruk.backend.shared.error.ServiceError

enum class Roles(val value: String) {
    Partner("partner"),
    RegEmployee("reg_employee"),
    ReuseStation("reuse_station")
}

object Authorization {

    fun authorizeRole(allowedRoles: List<Roles>, call: ApplicationCall): Either<AuthorizationError, Pair<Roles, Int>> {
        val principal = runCatching { call.principal<JWTPrincipal>()!! }.onFailure {
            return AuthorizationError.InvalidPrincipal().left()
        }.getOrElse { return AuthorizationError.InvalidPrincipal().left() }
        val claimRoles = principal.payload.claims["realm_access"]?.asMap()?.get("roles") as List<String>?
            ?: return AuthorizationError.MissingRolesError().left()
        val role = allowedRoles.firstOrNull { role -> claimRoles.any { it == role.value } }
            ?: return AuthorizationError.InsufficientRoleError().left()
        val groupID = principal.payload.claims["GroupID"]?.asInt() //-1 serves as a placeholder value for stations and REG admin
            ?: if (role != Roles.Partner) -1 else return AuthorizationError.MissingGroupIDError().left()
        return Pair(role, groupID).right()
    }

    fun authorizePartnerID(role: Pair<Roles, Int>, eventsFunc: () -> Either<ServiceError, List<Event>>) = eventsFunc()
        .flatMap {
            if (role.first == Roles.Partner && it.any { event -> event.partner.id != role.second }) {
                AuthorizationError.AccessViolationError().left()
            } else Unit.right()
        }
}