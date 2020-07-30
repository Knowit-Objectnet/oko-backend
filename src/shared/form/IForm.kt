package ombruk.backend.shared.form

import arrow.core.Either
import ombruk.backend.shared.error.ValidationError

interface IForm<T> {
    fun validOrError(): Either<ValidationError, T>
}
