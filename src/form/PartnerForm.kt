package ombruk.backend.form

import kotlinx.serialization.Serializable

@Serializable
data class PartnerForm(val id: Int = 0, var name: String)