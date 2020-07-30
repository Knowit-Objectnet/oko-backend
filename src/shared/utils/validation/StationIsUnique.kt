package ombruk.backend.shared.utils.validation

import arrow.core.getOrElse
import ombruk.backend.calendar.database.IStationRepository
import ombruk.backend.calendar.form.station.StationGetForm
import org.valiktor.Constraint
import org.valiktor.Validator

data class StationIsUnique(val repository: IStationRepository) : Constraint

fun <E> Validator<E>.Property<String?>.isStationUnique(repository: IStationRepository) =
    this.validate(StationIsUnique(repository)) {
        it == null || repository.getStations(StationGetForm(it)).map { it.isEmpty() }.getOrElse { true }
    }