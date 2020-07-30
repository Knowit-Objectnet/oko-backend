package ombruk.backend.calendar.database

import arrow.core.Either
import ombruk.backend.calendar.form.StationPostForm
import ombruk.backend.calendar.form.StationUpdateForm
import ombruk.backend.calendar.model.Station
import ombruk.backend.shared.database.IRepository
import ombruk.backend.shared.error.RepositoryError

interface IStationRepository : IRepository {

    fun getStationById(id: Int): Either<RepositoryError, Station>
    fun getStations(): Either<RepositoryError, List<Station>>
    fun insertStation(stationPostForm: StationPostForm): Either<RepositoryError, Station>
    fun updateStation(stationUpdateForm: StationUpdateForm): Either<RepositoryError, Station>
    fun deleteStation(id: Int): Either<RepositoryError, Int>
}