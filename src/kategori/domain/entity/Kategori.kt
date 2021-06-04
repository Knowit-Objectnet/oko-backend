package ombruk.backend.kategori.domain.entity

import kotlinx.serialization.Serializable
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class Kategori(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val navn: String
)