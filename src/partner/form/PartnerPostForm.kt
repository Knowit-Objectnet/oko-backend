package ombruk.backend.partner.form

import kotlinx.serialization.Serializable

@Serializable
data class PartnerPostForm(
    var name: String,
    var description: String,
    var phone: String,
    var email: String
)