package no.oslokommune.ombruk.shared.utils.validation

import org.valiktor.Constraint
import org.valiktor.Validator
import java.time.LocalTime

data class GreaterThanOpeningTime(val openingTime: LocalTime?) : Constraint

fun <E> Validator<E>.Property<LocalTime?>.isGreaterThanOpeningTime(openingTime: LocalTime?) =
    this.validate(GreaterThanOpeningTime(openingTime)) {
        it == null || openingTime == null || it > openingTime
    }

data class LessThanClosingTime(val closingTime: LocalTime?) : Constraint

fun <E> Validator<E>.Property<LocalTime?>.isLessThanClosingTime(closingTime: LocalTime?) =
    this.validate(LessThanClosingTime(closingTime)) {
        it == null || closingTime == null || it < closingTime
    }