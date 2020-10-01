package no.oslokommune.ombruk.shared.form

import arrow.core.Either
import no.oslokommune.ombruk.shared.error.ValidationError

interface IForm<T> {
    fun validOrError(): Either<ValidationError, T>
}
