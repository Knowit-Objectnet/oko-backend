package ombruk.backend.calendar.service

import io.ktor.locations.KtorExperimentalLocationsAPI
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.calendar.form.station.StationGetForm
import ombruk.backend.calendar.form.station.StationPostForm
import ombruk.backend.calendar.form.station.StationUpdateForm

object StationService : IStationService {

    override fun getStationById(id: Int) = StationRepository.getStationById(id)

    @KtorExperimentalLocationsAPI
    override fun getStations(stationGetForm: StationGetForm) = StationRepository.getStations(stationGetForm)

    override fun saveStation(stationPostForm: StationPostForm) = StationRepository.insertStation(stationPostForm)

    override fun updateStation(stationUpdateForm: StationUpdateForm) = StationRepository.updateStation(stationUpdateForm)

    override fun deleteStationById(id: Int) = StationRepository.deleteStation(id)

}