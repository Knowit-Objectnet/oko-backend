package ombruk.backend.calendar.service

import arrow.core.Either
import ombruk.backend.calendar.model.Station

interface IStationService {

    /**
     * Get a single station by it's id.
     * @return Either a throwable or a Station. The station mey be null if the id does not exist.
     */
    fun getStationById(id: Int): Either<Throwable, Station?>

    /**
     * Gets all stations
     * @return Either a throwable or a list of stations. The list may be empty if there are no stations
     */
    fun getStations(): Either<Throwable, List<Station>>
    
    /**
     * Saves a station.
     * @param station Station to save
     * @return Either a throwable or a the station that was saved
     */
    fun saveStation(station: Station): Either<Throwable, Station>

    /**
     * Update a station.
     * @param station Station to update
     * @return Either a Throwable or a the Station that was updated
     */
    fun updateStation(station: Station): Either<Throwable, Station>

    /**
     * Delete a station.
     * @param station Station to delete
     * @return Either a Throwable or the delete Station
     */
    fun deleteStationById(id: Int): Either<Throwable, Station>
}
