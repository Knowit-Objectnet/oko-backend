package ombruk.backend.notification.domain.params

import kotlinx.serialization.Serializable

@Serializable
data class SESCreateParams(
    val message: String,
    val addresses: List<String>
)