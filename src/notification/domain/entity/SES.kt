package ombruk.backend.notification.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class SES(
    val statusCode: Int? = 0,
    val message: String? = null
)