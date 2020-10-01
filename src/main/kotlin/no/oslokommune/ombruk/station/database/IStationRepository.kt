package no.oslokommune.ombruk.station.database

import arrow.core.Either
import no.oslokommune.ombruk.station.form.StationGetForm
import no.oslokommune.ombruk.station.form.StationPostForm
import no.oslokommune.ombruk.station.form.StationUpdateForm
import no.oslokommune.ombruk.station.model.Station
import no.oslokommune.ombruk.shared.database.IRepository
import no.oslokommune.ombruk.shared.database.IRepositoryUniqueName
import no.oslokommune.ombruk.shared.error.RepositoryError

interface IStationRepository : IRepository, IRepositoryUniqueName {

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