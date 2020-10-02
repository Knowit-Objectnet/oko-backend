package no.oslokommune.ombruk.uttaksdata.service

import arrow.core.Either
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.database.UttaksdataRepository
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.Uttaksdata
import no.oslokommune.ombruk.shared.error.ServiceError

object UttaksdataService : IUttaksdataService {

    override fun saveReport(uttak: Uttak): Either<ServiceError, Uttaksdata> = UttaksdataRepository.insertReport(uttak)

    override fun updateReport(uttak: Uttak): Either<ServiceError, Unit> = UttaksdataRepository.updateReport(uttak)

    override fun updateReport(uttaksdataUpdateForm: UttaksdataUpdateForm): Either<ServiceError, Uttaksdata> =
        UttaksdataRepository.updateReport(uttaksdataUpdateForm)

    override fun getReportById(uttaksdataID: Int): Either<ServiceError, Uttaksdata> = UttaksdataRepository.getReportByID(uttaksdataID)

    override fun getReports(uttaksdataGetForm: UttaksdataGetForm): Either<ServiceError, List<Uttaksdata>> =
        UttaksdataRepository.getReports(uttaksdataGetForm)


}