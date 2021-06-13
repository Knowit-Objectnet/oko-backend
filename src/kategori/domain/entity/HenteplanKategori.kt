package ombruk.backend.kategori.domain.entity

import kotlinx.serialization.Serializable
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class HenteplanKategori(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    @Serializable(with = UUIDSerializer::class) val hentplanId: UUID,
    @Serializable(with = UUIDSerializer::class) val kategoriId: UUID,
    val kategori: Kategori?,
    val merknad: String?
)