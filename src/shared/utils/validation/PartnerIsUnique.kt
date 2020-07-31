package ombruk.backend.shared.utils.validation

import arrow.core.getOrElse
import ombruk.backend.partner.database.IPartnerRepository
import ombruk.backend.partner.form.PartnerGetForm
import org.valiktor.Constraint
import org.valiktor.Validator

data class PartnerIsUnique(val repository: IPartnerRepository) : Constraint

fun <E> Validator<E>.Property<String?>.isPartnerUnique(repository: IPartnerRepository) =
    this.validate(PartnerIsUnique(repository)) {
        it == null || repository.getPartners(PartnerGetForm(it)).map { it.isEmpty() }.getOrElse { true }
    }