package ombruk.backend.kategori.application.api.dto

import kotlinx.serialization.Serializable
import shared.model.serializer.UUIDSerializer
import java.util.*

interface IKategoriKoblingSaveDto {
    @Serializable(with = UUIDSerializer::class) val kategoriId: UUID
}