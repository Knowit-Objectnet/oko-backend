package ombruk.backend.aktor.domain.entity

import kotlinx.serialization.Serializable
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class VerifiseringStatus (
    @Serializable (with = UUIDSerializer::class) val id: UUID,
    var telefonVerifisert: Boolean = false,
    var epostVerifisert: Boolean = false
)