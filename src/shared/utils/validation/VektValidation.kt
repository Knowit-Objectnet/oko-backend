package ombruk.backend.shared.utils.validation

import org.valiktor.Constraint
import org.valiktor.Validator

object PositiveOrZeroList : Constraint

fun <E> Validator<E>.Property<Iterable<Float>?>.isPositiveOrZeroList() =
    this.validate(PositiveOrZeroList) { it == null || it.all { it >= 0 }}

object VektEqualSizeOfIDList : Constraint

fun <E> Validator<E>.Property<Iterable<Float>?>.equalSizeOfIDList(list: Iterable<String>) =
    this.validate(VektEqualSizeOfIDList) {  it == null || it.count() == list.count() }