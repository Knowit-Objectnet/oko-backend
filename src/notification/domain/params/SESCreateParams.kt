package ombruk.backend.notification.domain.params

import kotlinx.serialization.Serializable

@Serializable
data class SESCreateParams(
    val subject: String,
    val previewMessage: String,
    val message: String,
    val addresses: List<String>
)