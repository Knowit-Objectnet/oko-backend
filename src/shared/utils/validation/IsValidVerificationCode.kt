package ombruk.backend.shared.utils.validation

import org.valiktor.Constraint
import org.valiktor.Validator


object VerificationCode : Constraint

fun <E> Validator<E>.Property<String?>.isValidVerificationCode() =
    this.validate(VerificationCode) {
        it == null || Regex("^\\d{6}$") matches it
    }