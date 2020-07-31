package ombruk.backend.calendar.database

import arrow.core.Either
import ombruk.backend.calendar.form.station.StationGetForm
import ombruk.backend.calendar.form.station.StationPostForm
import ombruk.backend.calendar.form.station.StationUpdateForm
import ombruk.backend.calendar.model.Station
import ombruk.backend.shared.database.IRepository
import ombruk.backend.shared.error.RepositoryError

interface IStationRepository : IRepository {

    /**
     * Gets a station by its ID.
     *
     * @param id Id of the station to get
     * @return A [RepositoryError] on failure or a [Station] on success.
     */
    fun getStationById(id: Int): Either<RepositoryError, Station>

    /**
     * Gets a list of stations filtered by parameters specified in a [StationGetForm].
     *
     * @param stationGetForm A [StationGetForm] used for specifying query conditionals.
     * @return A [RepositoryError] on failure and a [List] of [Station] objects on success.
     */
    fun getStations(stationGetForm: StationGetForm): Either<RepositoryError, List<Station>>

    /**
     * Insert a Station to the db.
     *
     * @param stationPostForm The Station to insert
     * @return A [RepositoryError] on failure and the inserted [Station] on success.
     */
    fun insertStation(stationPostForm: StationPostForm): Either<RepositoryError, Station>

    /**
     * Update a given Station.
     *
     * @param stationUpdateForm A [StationUpdateForm] containing values to be updated. Properties that are null will not be updated.
     * @return A [RepositoryError] on failure and a [Station] on success.
     */
    fun updateStation(stationUpdateForm: StationUpdateForm): Either<RepositoryError, Station>

    /**
     * Delete a given station from the DB.
     *
     * @param id The id of the [Station] to delete.
     * @return A [RepositoryError] on success and an [Int] on success. The [Int] represents the amount of stations that were deleted.
     */
    fun deleteStation(id: Int): Either<RepositoryError, Int>
}