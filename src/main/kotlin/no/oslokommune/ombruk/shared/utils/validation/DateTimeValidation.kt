package no.oslokommune.ombruk.shared.utils.validation

import org.valiktor.Constraint
import org.valiktor.Validator
import java.time.LocalDateTime

data class GreaterThanStartDateTime(val startDateTime: LocalDateTime?) : Constraint

fun <E> Validator<E>.Property<LocalDateTime?>.isGreaterThanStartDateTime(startDateTime: LocalDateTime?) =
    this.validate(GreaterThanStartDateTime(startDateTime)) {
        it == null || startDateTime == null || it > startDateTime
    }

data class LessThanEndDateTime(val endDateTime: LocalDateTime?) : Constraint

fun <E> Validator<E>.Property<LocalDateTime?>.isLessThanEndDateTime(endDateTime: LocalDateTime?) =
    this.validate(LessThanEndDateTime(endDateTime)) {
        it == null || endDateTime == null || it < endDateTime
    }


data class SameDate(val endDateTime: LocalDateTime?) : Constraint

fun <E> Validator<E>.Property<LocalDateTime?>.isSameDateAs(endDateTime: LocalDateTime?) =
    this.validate(SameDate(endDateTime)) {
        it == null || endDateTime == null || it.toLocalDate() == endDateTime.toLocalDate()
    }

