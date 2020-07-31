package ombruk.backend.calendar.service

import arrow.core.Either
import ombruk.backend.calendar.form.station.StationGetForm
import ombruk.backend.calendar.form.station.StationPostForm
import ombruk.backend.calendar.form.station.StationUpdateForm
import ombruk.backend.calendar.model.Station
import ombruk.backend.shared.error.ServiceError

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
     * @return Either a [ServiceError] or the amount of [Station] objects that were deleted.
     */
    fun deleteStationById(id: Int): Either<ServiceError, Int>
}
