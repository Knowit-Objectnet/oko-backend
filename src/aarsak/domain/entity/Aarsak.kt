package ombruk.backend.aarsak.domain.entity

import kotlinx.serialization.Serializable
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class Aarsak (
    @Serializable (with = UUIDSerializer::class) val id: UUID,
    var beskrivelse: String
)