package ombruk.backend.statistikk.domain.entity

import kotlinx.serialization.Serializable
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class Kategori(
    @Serializable(with = UUIDSerializer::class) val kategoriId: UUID = UUID.randomUUID(),
    val kategoriNavn: String = "KategoriNavn",
    val vekt: Float = 666f,
)