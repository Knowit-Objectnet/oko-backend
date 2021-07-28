package ombruk.backend.shared.utils.validation

import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import org.valiktor.Constraint
import org.valiktor.Validator
import java.util.*

object EkstrahentingGodkjent: Constraint

fun <E> Validator<E>.Property<UUID?>.isValidEkstrahenting(ekstrahenting: EkstraHenting) =
    this.validate(EkstrahentingGodkjent) {ekstrahenting.godkjentUtlysning != null}

object NotAvlyst: Constraint

fun <E> Validator<E>.Property<UUID?>.isNotAvlyst(planlagtHenting: PlanlagtHenting) =
    this.validate(NotAvlyst) {planlagtHenting.avlyst == null}
