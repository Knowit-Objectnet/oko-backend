package ombruk.backend.aktor.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Kontakt (
    val id: Int,
    val navn: String,
    val telefon: String,
    val rolle: String
)