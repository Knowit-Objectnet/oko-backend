package ombruk.backend.kategori.domain.params

import java.util.*

abstract class HenteplanKategoriCreateParams {
    //abstract val id: UUID?
    abstract val henteplanId: UUID
    abstract val kategoriId: UUID
    abstract val merknad: String?
}