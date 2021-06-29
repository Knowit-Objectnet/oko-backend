package ombruk.backend.notification.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class SNS(
    val statusCode: Int? = 0,
    val message: String? = null
)