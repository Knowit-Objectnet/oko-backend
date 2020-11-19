package no.oslokommune.ombruk.stasjon.service

import arrow.core.Either
import arrow.core.flatMap
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.stasjon.form.StasjonGetForm
import no.oslokommune.ombruk.stasjon.form.StasjonPostForm
import no.oslokommune.ombruk.stasjon.form.StasjonUpdateForm
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.shared.api.KeycloakGroupIntegration
import no.oslokommune.ombruk.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction

object StasjonService : IStasjonService {

    override fun getStasjonById(id: Int): Either<ServiceError, Stasjon> = StasjonRepository.getStasjonById(id)

    @KtorExperimentalLocationsAPI
    override fun getStasjoner(stasjonGetForm: StasjonGetForm): Either<ServiceError, List<Stasjon>> =
        StasjonRepository.getStasjoner(stasjonGetForm)

    @KtorExperimentalAPI
    override fun saveStasjon(stasjonPostForm: StasjonPostForm): Either<ServiceError, Stasjon> = transaction {
        StasjonRepository.insertStasjon(stasjonPostForm).flatMap { stasjon ->
            KeycloakGroupIntegration.createGroup(stasjon.navn, stasjon.id)
                .bimap({ rollback(); it }, { stasjon })
        }
    }

    @KtorExperimentalAPI
    override fun updateStasjon(stasjonUpdateForm: StasjonUpdateForm): Either<ServiceError, Stasjon> = transaction {
        getStasjonById(stasjonUpdateForm.id).flatMap { stasjon ->
            StasjonRepository.updateStasjon(stasjonUpdateForm).flatMap { newStasjon ->
                KeycloakGroupIntegration.updateGroup(stasjon.navn, newStasjon.navn)
                    .bimap({ rollback(); it }, { newStasjon })
            }
        }
    }

    @KtorExperimentalAPI
    override fun deleteStasjonById(id: Int): Either<ServiceError, Stasjon> = transaction {
        getStasjonById(id).flatMap { stasjon ->
            StasjonRepository.deleteStasjon(id)
                .flatMap { KeycloakGroupIntegration.deleteGroup(stasjon.navn) }
                .bimap({ rollback(); it }, { stasjon })
        }
    }
}