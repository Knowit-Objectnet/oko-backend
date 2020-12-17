package no.oslokommune.ombruk.uttak.service

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.either.monad.flatMap
import arrow.core.extensions.list.traverse.sequence
import arrow.core.extensions.option.apply.map
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.uttak.database.GjentakelsesRegelTable
import no.oslokommune.ombruk.uttak.form.UttakGetForm
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.form.UttakUpdateForm
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.uttak.form.UttakDeleteForm
import no.oslokommune.ombruk.uttaksdata.database.UttaksDataRepository
import no.oslokommune.ombruk.uttaksdata.service.UttaksDataService
import org.jetbrains.exposed.sql.transactions.transaction

object UttakService : IUttakService {

    // TODO: Return all created uttak

    private fun saveHelper(uttakPostForm: UttakPostForm): Either<ServiceError, Uttak> = transaction {
        uttakPostForm.map { form ->
            UttakRepository.insertUttak(form)
                .flatMap { uttak ->
                    UttaksDataService.saveUttaksData(uttak)
                        .map { uttak.uttaksData = it; uttak }
                        .fold({ it.left() }, { uttak.right() })
                }
        }.sequence(Either.applicative()).fix()
            .map { it.fix() }
            .fold({ rollback(); it.left() }, { it.first().right() })
    }

    override fun saveUttak(uttakPostForm: UttakPostForm): Either<ServiceError, Uttak> = transaction {
        let {
            uttakPostForm.gjentakelsesRegel?.let { GjentakelsesRegelTable.insertGjentakelsesRegel(it) } ?: Unit.right()
        }
            .flatMap { saveHelper(uttakPostForm) }
            .map { println("Saved helper"); it }
            .fold({ rollback(); it.left() }, { it.right() })
//        uttakPostForm.gjentakelsesRegel?.let { gjenRegel ->
//
//            // Generates forms using it.gjentakelsesRegel
//            val generatedForms = uttakPostForm.map { form -> form }
//            if (gjenRegel.until == null)
//                gjenRegel.until = generatedForms.last().sluttTidspunkt
//            if (gjenRegel.antall == null)
//                gjenRegel.antall = generatedForms.size
//
//            // Insert gjentakelsesregel and get id
//            val id = GjentakelsesRegelTable.insertGjentakelsesRegel(gjenRegel)
//                .map { gjRegel -> gjRegel.id!! }.getOrElse { -1 } // TODO: Dysfunctional
//
//            // Update generated forms with id and insert them
//            generatedForms.map { form ->
//                form.gjentakelsesRegel!!.id = id
//                UttakRepository.insertUttak(form)
//            }.first()
//
//        } ?: UttakRepository.insertUttak(uttakPostForm)
//        .fold({ rollback(); it.left() }, { it.right() })
    }

    override fun getUttakByID(id: Int): Either<ServiceError, Uttak> = transaction {
        UttakRepository.getUttakByID(id)
    }

    override fun getUttak(uttakGetForm: UttakGetForm?): Either<ServiceError, List<Uttak>> = transaction {
        UttakRepository.getUttak(uttakGetForm)
    }

    @KtorExperimentalLocationsAPI
    override fun deleteUttak(uttakDeleteForm: UttakDeleteForm): Either<ServiceError, List<Uttak>> = transaction {
        UttakRepository.deleteUttak(uttakDeleteForm)
    }

    override fun updateUttak(uttakUpdate: UttakUpdateForm): Either<ServiceError, Uttak> = transaction {
        UttakRepository.updateUttak(uttakUpdate)
            .fold({ rollback(); it.left() }, { it.right() })
    }

}