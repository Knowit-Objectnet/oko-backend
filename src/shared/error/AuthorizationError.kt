package ombruk.backend.shared.error


sealed class AuthorizationError(msg: String) : ServiceError(msg) {
    data class MissingRolesError(val reason: String = "") : AuthorizationError("No OKO roles provided, $reason")
    data class InsufficientRoleError(val reason: String = "") :
        AuthorizationError("Provided roles are not sufficient for API call, $reason")

    data class MissingGroupIDError(val reason: String = "") :
        AuthorizationError("JWT token is missing a Group ID, $reason")

    data class InvalidPrincipal(val reason: String = "") : AuthorizationError("JWT token is mangled, $reason")
    data class AccessViolationError(val reason: String = "") :
        AuthorizationError("The requested events do not belong to you $reason")
}