package ombruk.backend.aktor.domain.entity

import kotlinx.serialization.Serializable
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable(with = UUIDSerializer::class)
data class Kontakt (
    val id: UUID,
    val navn: String,
    val telefon: String,
    val rolle: String
)