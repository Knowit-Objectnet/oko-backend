package ombruk.backend.shared.api

import kotlinx.serialization.Serializable

@Serializable
data class KeycloakPartner(
    val id: String,
    val name: String,
    val path: String,
    val subGroups: List<Unit>
)