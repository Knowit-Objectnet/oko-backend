package ombruk.backend.aktor.domain.model

import ombruk.backend.core.domain.model.CreateParams
import java.util.*


abstract class KontaktCreateParams : CreateParams {
    abstract val aktorId: UUID
    abstract val navn: String
    abstract val telefon: String?
    abstract val epost: String?
    abstract val rolle: String?
}