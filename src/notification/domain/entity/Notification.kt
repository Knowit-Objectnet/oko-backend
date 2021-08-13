package ombruk.backend.notification.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val message: String? = null
)