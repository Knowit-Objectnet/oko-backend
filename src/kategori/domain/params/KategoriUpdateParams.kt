package ombruk.backend.kategori.domain.params

import ombruk.backend.core.domain.model.UpdateParams
import java.util.*

abstract class KategoriUpdateParams : UpdateParams {
    abstract override val id: UUID
    abstract val navn: String?
}