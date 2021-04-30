package ombruk.backend.aktor.domain.model

import ombruk.backend.core.domain.model.CreateParams


abstract class KontaktCreateParams : CreateParams {
    abstract val navn: String
    abstract val telefon: String
    abstract val rolle: String
}