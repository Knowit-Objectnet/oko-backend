package no.oslokommune.ombruk.uttaksdata.service

import arrow.core.Either
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.uttaksdata.database.UttaksDataRepository
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.Uttaksdata
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataPostForm

object UttaksDataService : IUttaksDataService {

    override fun saveUttaksdata(form: UttaksdataPostForm): Either<ServiceError, Uttaksdata> =
        UttaksDataRepository.insertUttaksdata(form)

    override fun saveUttaksData(uttak: Uttak): Either<ServiceError, Uttaksdata> {
        return UttaksDataRepository.insertUttaksdata(uttak)
    }

    override fun updateUttaksdata(form: UttaksdataUpdateForm): Either<RepositoryError, Uttaksdata> =
        UttaksDataRepository.updateUttaksdata(form)

    override fun getUttaksdataById(uttaksdataID: Int): Either<ServiceError, Uttaksdata> =
        UttaksDataRepository.getUttaksDataByID(uttaksdataID)

    override fun getUttaksdata(form: UttaksdataGetForm): Either<ServiceError, List<Uttaksdata>> =
        UttaksDataRepository.getUttaksData(form)

    override fun deleteByUttakId(uttakId: Int): Either<ServiceError, Unit> =
        UttaksDataRepository.deleteByUttakId(uttakId)

}