package ombruk.backend.statistikk.domain.entity

import kotlinx.serialization.Serializable
import shared.model.serializer.UUIDSerializer
import java.util.*

@Serializable
data class KategoriStatistikk(
    @Serializable(with = UUIDSerializer::class) val kategoriId: UUID? = null,
    val kategoriNavn: String = "",
    val vekt: Float = 0f,
)