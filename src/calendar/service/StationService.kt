package ombruk.backend.calendar.service

import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.calendar.form.StationPostForm
import ombruk.backend.calendar.form.StationUpdateForm

object StationService : IStationService {

    override fun getStationById(id: Int) = StationRepository.getStationById(id)

    override fun getStations() = StationRepository.getStations()

    override fun saveStation(stationPostForm: StationPostForm) = StationRepository.insertStation(stationPostForm)

    override fun updateStation(stationUpdateForm: StationUpdateForm) = StationRepository.updateStation(stationUpdateForm)

    override fun deleteStationById(id: Int) = StationRepository.deleteStation(id)

}