package ombruk.backend.shared.utils.validation

import org.valiktor.Constraint
import org.valiktor.Validator
import java.time.DayOfWeek
import java.time.LocalTime

data class OpenHoursIsValid(val hours: Map<DayOfWeek, List<LocalTime>>?) : Constraint

fun <E> Validator<E>.Property<Map<DayOfWeek, List<LocalTime>>?>.isValid() =
    this.validate(OpenHoursIsValid(null)) {
        it == null || it.all { it.key.value in 1..5 && it.value.size == 2 && it.value[0] < it.value[1] }
    }