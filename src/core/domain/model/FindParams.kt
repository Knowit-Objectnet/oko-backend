package ombruk.backend.core.domain.model

import java.util.*

interface FindParams {
    val id: UUID?
    val arkivert: Boolean
        get() = false
}