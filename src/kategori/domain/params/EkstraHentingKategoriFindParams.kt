package ombruk.backend.kategori.domain.params

import ombruk.backend.core.domain.model.FindParams
import java.util.*

abstract class EkstraHentingKategoriFindParams : FindParams {
    //abstract override val id: UUID?
    abstract val ekstraHentingId: UUID?
    abstract val kategoriId: UUID?
    abstract val mengde: Float?
}