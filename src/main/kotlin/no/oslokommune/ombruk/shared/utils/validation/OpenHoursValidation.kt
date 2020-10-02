package no.oslokommune.ombruk.shared.utils.validation

import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import org.valiktor.Constraint
import org.valiktor.Validator
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

data class OpenHoursIsValid(val hours: Map<DayOfWeek, List<LocalTime>>?) : Constraint

data class TimeIsWithinOpeningHours(val stasjonId: Int) : Constraint

fun <E> Validator<E>.Property<Map<DayOfWeek, List<LocalTime>>?>.isValid() =
    this.validate(OpenHoursIsValid(null)) {
        it == null || it.all { it.key.value in 1..5 && it.value.size == 2 && it.value[0] < it.value[1] }
    }

fun <E> Validator<E>.Property<LocalDateTime?>.isWithinOpeningHoursOf(stasjonId: Int) =
    this.validate(TimeIsWithinOpeningHours(stasjonId)) { dateTime ->
        dateTime == null ||
                StasjonRepository.getStasjonById(stasjonId).exists { stasjon -> dateTime.isWithin(stasjon.hours!!) }
    }

fun LocalDateTime.isWithin(openHours: Map<DayOfWeek, List<LocalTime>>): Boolean =
    openHours.any { map ->
        this.dayOfWeek == map.key &&
                map.value.first().isBefore(this.toLocalTime()) &&
                map.value.last().isAfter(this.toLocalTime())
    }