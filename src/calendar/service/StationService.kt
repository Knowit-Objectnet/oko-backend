package ombruk.backend.calendar.service

import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.typesafe.config.ConfigFactory
import io.ktor.config.HoconApplicationConfig
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.calendar.form.station.StationGetForm
import ombruk.backend.calendar.form.station.StationPostForm
import ombruk.backend.calendar.form.station.StationUpdateForm
import ombruk.backend.shared.api.KeycloakGroupIntegration
import org.jetbrains.exposed.sql.transactions.transaction

object StationService : IStationService {

    @KtorExperimentalAPI
    private val appConfig = HoconApplicationConfig(ConfigFactory.load())

    @KtorExperimentalAPI
    private val isDebug: Boolean = appConfig.property("ktor.oko.debug").getString().toBoolean()


    override fun getStationById(id: Int) = StationRepository.getStationById(id)

    @KtorExperimentalLocationsAPI
    override fun getStations(stationGetForm: StationGetForm) = StationRepository.getStations(stationGetForm)

    @KtorExperimentalAPI
    override fun saveStation(stationPostForm: StationPostForm) = transaction {
        val station = StationRepository.insertStation(stationPostForm)
            .fold({ return@transaction it.left() }, { it })

        takeIf { !isDebug }?.let {
            KeycloakGroupIntegration.createGroup(station.name, station.id)
                .bimap({ rollback(); it }, { station })
        } ?: station.right()
    }

    @KtorExperimentalAPI
    override fun updateStation(stationUpdateForm: StationUpdateForm) = transaction {
        val station = getStationById(stationUpdateForm.id)
            .fold({ return@transaction it.left() }, { it })

        takeIf { isDebug || stationUpdateForm.name == null }?.let { StationRepository.updateStation(stationUpdateForm) }
            ?: StationRepository.updateStation(stationUpdateForm)
                .flatMap { newStation ->
                    KeycloakGroupIntegration.updateGroup(station.name, newStation.name)
                        .bimap({ rollback(); it }, { newStation })
                }
    }


    @KtorExperimentalAPI
    override fun deleteStationById(id: Int) = transaction {
        val station = getStationById(id)
            .fold({ return@transaction it.left() }, { it })

        takeIf { !isDebug }?.let {
            StationRepository.deleteStation(id)
                .flatMap { KeycloakGroupIntegration.deleteGroup(station.name) }
                .bimap({rollback(); it}, {station})
        } ?: StationRepository.deleteStation(id)
            .fold({it.left()}, {station.right()})
    }
}