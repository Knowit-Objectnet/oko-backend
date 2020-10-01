package no.oslokommune.ombruk.station.service

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.station.form.StationGetForm
import no.oslokommune.ombruk.station.form.StationPostForm
import no.oslokommune.ombruk.station.form.StationUpdateForm
import no.oslokommune.ombruk.station.model.Station
import no.oslokommune.ombruk.shared.error.ServiceError

interface IStationService {

    /**
     * Get a single station by its id.
     * @return Either a [ServiceError] or a [Station].
     */
    fun getStationById(id: Int): Either<ServiceError, Station>

    /**
     * Gets all stations
     * @return Either a [ServiceError] or a [List] of [Station] objects. The list may be empty if there are no stations
     */
    @KtorExperimentalLocationsAPI
    fun getStations(stationGetForm: StationGetForm): Either<ServiceError, List<Station>>

    /**
     * Saves a station.
     * @param stationPostForm Station to save
     * @return Either a [ServiceError] or the saved [Station]
     */
    fun saveStation(stationPostForm: StationPostForm): Either<ServiceError, Station>

    /**
     * Update a station.
     * @param stationUpdateForm Station to update
     * @return Either a [ServiceError] or the updated [Station]
     */
    fun updateStation(stationUpdateForm: StationUpdateForm): Either<ServiceError, Station>

    /**
     * Delete a station.
     * @param id ID of station to delete
     * @return A [ServiceError] on failure and the deleted [Station] on success.
     */
    fun deleteStationById(id: Int): Either<ServiceError, Station>
}
