package ombruk.backend.notification.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Verification(
    val message: String? = null
)