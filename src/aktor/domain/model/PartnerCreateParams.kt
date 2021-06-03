package ombruk.backend.aktor.domain.model

abstract class PartnerCreateParams {
    abstract val navn: String
    abstract val ideell: Boolean
}