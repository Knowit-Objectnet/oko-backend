package ombruk.backend.notification.domain.params

import kotlinx.serialization.Serializable

@Serializable
data class SNSCreateParams(
    val subject: String,
    val message: String,
    val number: String
)