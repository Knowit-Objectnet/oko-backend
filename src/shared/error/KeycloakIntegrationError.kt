package ombruk.backend.shared.error

sealed class KeycloakIntegrationError(msg: String = "") : ServiceError(msg){
    data class ConflictError(val reason: String = ""): KeycloakIntegrationError("A group with that name already exists in keycloak, $reason")
    data class AuthenticationError(val reason: String = ""): KeycloakIntegrationError("Failed to authenticate, $reason")
    data class KeycloakError(val reason: String = ""): KeycloakIntegrationError("Failed to connect to keycloak, $reason")
    data class NotFoundError(val reason: String = ""): KeycloakIntegrationError("ID does not exist, $reason")
}