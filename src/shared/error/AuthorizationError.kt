package ombruk.backend.shared.error


sealed class AuthorizationError(msg: String) : ServiceError(msg) {
    data class MissingRolesError(val reason: String = "") : AuthorizationError("Ingen OKO-roller gitt, $reason")
    data class InsufficientRoleError(val reason: String = "") :
        AuthorizationError("Dine roller gir deg ikke tilgang til dette API-kallet: $reason")

    data class MissingGroupIDError(val reason: String = "") :
        AuthorizationError("JWT token mangler GroupId, $reason")

    data class InvalidPrincipal(val reason: String = "") : AuthorizationError("JWT token er ødelagt, $reason")
    data class AccessViolationError(val reason: String = "") :
        AuthorizationError("Ingen tilgang: $reason")
}