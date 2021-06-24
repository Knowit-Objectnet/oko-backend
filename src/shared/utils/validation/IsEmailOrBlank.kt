package ombruk.backend.shared.utils.validation

import org.valiktor.Constraint
import org.valiktor.Validator
import org.valiktor.constraints.Email
import org.valiktor.functions.isEmail


object EmailOrBlank : Constraint

fun <E> Validator<E>.Property<String?>.isEmailOrBlank() =
    this.validate(EmailOrBlank) {
        it == null || it.isBlank() || it.matches(Regex(
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"))
    }