package ombruk.backend.statistikk.domain.params

import java.time.LocalDateTime
import java.util.*

abstract class StatistikkFindParams {
    abstract val partnerId: UUID?
    abstract val stasjonId: UUID?
    abstract val kategoriId: UUID?
    abstract val before: LocalDateTime?
    abstract val after: LocalDateTime?
}