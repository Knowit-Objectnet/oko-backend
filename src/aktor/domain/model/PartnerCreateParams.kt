package ombruk.backend.aktor.domain.model

import java.util.*

abstract class PartnerCreateParams {
    abstract val id: UUID?
    abstract val navn: String
    abstract val ideell: Boolean
}