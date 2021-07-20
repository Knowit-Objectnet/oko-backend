package ombruk.backend.notification.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class VerificationMessage(
    val message: String? = null
)