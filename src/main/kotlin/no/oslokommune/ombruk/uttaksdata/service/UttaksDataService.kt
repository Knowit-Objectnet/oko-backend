package no.oslokommune.ombruk.uttaksdata.service

import arrow.core.Either
import no.oslokommune.ombruk.uttaksdata.database.UttaksDataRepository
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.UttaksData
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.uttak.model.Uttak

object UttaksDataService : IUttaksDataService {

    override fun saveUttaksData(uttak: Uttak): Either<ServiceError, UttaksData> {
        return UttaksDataRepository.insertUttaksdata(uttak)
    }

    override fun updateUttaksData(form: UttaksDataUpdateForm): Either<ServiceError, UttaksData> =
        UttaksDataRepository.updateUttaksData(form)

    override fun getUttaksDataById(uttaksDataId: Int): Either<ServiceError, UttaksData> =
        UttaksDataRepository.getUttaksDataById(uttaksDataId)

    override fun getUttaksData(form: UttaksDataGetForm): Either<ServiceError, List<UttaksData>> =
        UttaksDataRepository.getUttaksData(form)

}