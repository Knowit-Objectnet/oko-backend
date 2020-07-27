package ombruk.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class Partner(val id: Int, var name: String)