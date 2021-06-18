package ombruk.backend.kategori.domain.params

import java.util.*

abstract class EkstraHentingKategoriCreateParams {
    abstract val ekstraHentingId: UUID
    abstract val kategoriId: UUID
    abstract val mengde: Float?
}