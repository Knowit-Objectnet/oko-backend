package ombruk.backend.form

import kotlinx.serialization.Serializable

@Serializable
data class KeycloakPartnerForm(
    val id: String,
    val name: String,
    val path: String,
    val subGroups: List<Unit>
)