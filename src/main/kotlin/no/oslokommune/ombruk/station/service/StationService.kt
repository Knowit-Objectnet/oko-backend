package no.oslokommune.ombruk.station.service

import arrow.core.Either
import arrow.core.flatMap
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import no.oslokommune.ombruk.station.database.StationRepository
import no.oslokommune.ombruk.station.form.StationGetForm
import no.oslokommune.ombruk.station.form.StationPostForm
import no.oslokommune.ombruk.station.form.StationUpdateForm
import no.oslokommune.ombruk.station.model.Station
import no.oslokommune.ombruk.shared.api.KeycloakGroupIntegration
import no.oslokommune.ombruk.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction

object StationService : IStationService {

    override fun getStationById(id: Int): Either<ServiceError, Station> = StationRepository.getStationById(id)

    @KtorExperimentalLocationsAPI
    override fun getStations(stationGetForm: StationGetForm): Either<ServiceError, List<Station>> =
        StationRepository.getStations(stationGetForm)

    @KtorExperimentalAPI
    override fun saveStation(stationPostForm: StationPostForm): Either<ServiceError, Station> = transaction {
        StationRepository.insertStation(stationPostForm).flatMap { station ->
            KeycloakGroupIntegration.createGroup(station.name, station.id)
                .bimap({ rollback(); it }, { station })
        }
    }

    @KtorExperimentalAPI
    override fun updateStation(stationUpdateForm: StationUpdateForm): Either<ServiceError, Station> = transaction {
        getStationById(stationUpdateForm.id).flatMap { station ->
            StationRepository.updateStation(stationUpdateForm).flatMap { newStation ->
                KeycloakGroupIntegration.updateGroup(station.name, newStation.name)
                    .bimap({ rollback(); it }, { newStation })
            }
        }
    }

    @KtorExperimentalAPI
    override fun deleteStationById(id: Int): Either<ServiceError, Station> = transaction {
        getStationById(id).flatMap { station ->
            StationRepository.deleteStation(id)
                .flatMap { KeycloakGroupIntegration.deleteGroup(station.name) }
                .bimap({ rollback(); it }, { station })
        }
    }
}