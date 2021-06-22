package testutils

import arrow.core.Either
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm

fun <T> IForm<T>.validateAndRequireRight(): T {
    val validated = this.validOrError()
    require(validated is Either.Right)
    return validated.b
}

fun <T> IForm<T>.validateAndRequireLeft(): ValidationError {
    val validated = this.validOrError()
    require(validated is Either.Left)
    return validated.a
}
