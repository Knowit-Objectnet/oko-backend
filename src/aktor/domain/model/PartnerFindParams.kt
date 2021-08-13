package ombruk.backend.aktor.domain.model

import ombruk.backend.core.domain.model.FindParams

abstract class PartnerFindParams : FindParams {
    abstract val navn: String?
    abstract val ideell: Boolean?
}