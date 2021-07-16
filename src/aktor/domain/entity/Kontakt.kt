package ombruk.backend.aktor.domain.entity

import kotlinx.serialization.Serializable
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class Kontakt (
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    @Serializable(with = UUIDSerializer::class) val aktorId: UUID,
    val navn: String,
    val telefon: String? = null,
    val epost: String? = null,
    val rolle: String? = null,
    val verifisert: Verifisert? = null
)