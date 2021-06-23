package ombruk.backend.shared.utils.validation

import org.valiktor.Constraint
import org.valiktor.Validator


object NorwegianPhoneNumber : Constraint

fun <E> Validator<E>.Property<String?>.isNorwegianPhoneNumber() =
    this.validate(NorwegianPhoneNumber) {
        it == null || Regex("^\\+47(4|9)\\d{7}$") matches it
    }