package ombruk.backend.shared.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Object used for OAuth response deserialization
 */
@Serializable
data class TokenResponse(
    //The SerialName annotation is used to convert JSON names to the ones used internally in the application, e.g "access_token" to "accessToken"
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("refresh_expires_in") val refreshExpiresIn: Int,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("not-before-policy") val notBeforePolicy: Int,
    @SerialName("session_state") val sessionState: String,
    val scope: String
)