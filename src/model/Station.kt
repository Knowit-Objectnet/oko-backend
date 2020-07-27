package ombruk.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class Station(val id: Int, var name: String)