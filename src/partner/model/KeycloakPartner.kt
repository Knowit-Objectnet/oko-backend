package ombruk.backend.partner.model

import kotlinx.serialization.Serializable

@Serializable
data class KeycloakPartner(
    val id: String,
    val name: String,
    val path: String,
    val subGroups: List<Unit>
)