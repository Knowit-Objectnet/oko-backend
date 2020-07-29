package ombruk.backend.partner.form

import kotlinx.serialization.Serializable

@Serializable
data class PartnerUpdateForm(
    val id: Int,
    var name: String? = null,
    var description: String? = null,
    var phone: String? = null,
    var email: String? = null
)