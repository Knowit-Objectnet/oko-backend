package no.oslokommune.ombruk.uttaksdata.service

import arrow.core.Either
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.uttaksdata.database.UttaksdataRepository
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.Uttaksdata
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataPostForm

object UttaksdataService : IUttaksdataService {

    override fun saveUttaksdata(form: UttaksdataPostForm): Either<ServiceError, Uttaksdata> = UttaksdataRepository.insertUttaksdata(form)

    override fun updateUttaksdata(form: UttaksdataUpdateForm): Either<RepositoryError, Uttaksdata> = UttaksdataRepository.updateUttaksdata(form)

    override fun getUttaksdataById(uttaksdataID: Int): Either<ServiceError, Uttaksdata> = UttaksdataRepository.getUttaksDataByID(uttaksdataID)

    override fun getUttaksdata(form: UttaksdataGetForm): Either<ServiceError, List<Uttaksdata>> =
        UttaksdataRepository.getUttaksData(form)


}