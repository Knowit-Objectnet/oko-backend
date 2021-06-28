package ombruk.backend.notification.domain.params

import kotlinx.serialization.Serializable

@Serializable
data class SNSCreateParams(
    val message: String,
    val number: String
)