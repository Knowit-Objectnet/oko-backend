package no.oslokommune.ombruk.partner.model

import kotlinx.serialization.Serializable

@Serializable
data class Partner(
    val id: Int,
    var name: String,
    var description: String? = null,
    var phone: String? = null,
    var email: String? = null
)