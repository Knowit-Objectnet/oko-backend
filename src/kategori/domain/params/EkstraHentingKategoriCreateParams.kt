package ombruk.backend.kategori.domain.params

import java.util.*

abstract class EkstraHentingKategoriCreateParams: KobletKategoriCreateParams() {
    abstract val ekstraHentingId: UUID
    abstract val mengde: Float?
}