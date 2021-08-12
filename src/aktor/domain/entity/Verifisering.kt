package ombruk.backend.aktor.domain.entity

import kotlinx.serialization.Serializable
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class Verifisering (
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val telefonKode: String? = null,
    var telefonVerifisert: Boolean = false,
    val epostKode: String? = null,
    var epostVerifisert: Boolean = false
)