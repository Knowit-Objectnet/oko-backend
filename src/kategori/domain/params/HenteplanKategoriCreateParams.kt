package ombruk.backend.kategori.domain.params

import java.util.*

abstract class HenteplanKategoriCreateParams: KobletKategoriCreateParams() {
    abstract val henteplanId: UUID
}