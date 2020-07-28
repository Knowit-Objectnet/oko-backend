package ombruk.backend.partner.model

import kotlinx.serialization.Serializable

@Serializable
data class Partner(
    val id: Int,
    var name: String,
    var description: String?,
    var phone: String?,
    var email: String?
)