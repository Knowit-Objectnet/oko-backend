package no.oslokommune.ombruk.uttaksdata.service

import arrow.core.Either
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.database.ReportRepository
import no.oslokommune.ombruk.uttaksdata.form.ReportGetForm
import no.oslokommune.ombruk.uttaksdata.form.ReportUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.Report
import no.oslokommune.ombruk.shared.error.ServiceError

object ReportService : IReportService {

    override fun saveReport(uttak: Uttak): Either<ServiceError, Report> = ReportRepository.insertReport(uttak)

    override fun updateReport(uttak: Uttak): Either<ServiceError, Unit> = ReportRepository.updateReport(uttak)

    override fun updateReport(uttaksdataUpdateForm: ReportUpdateForm): Either<ServiceError, Report> =
        ReportRepository.updateReport(uttaksdataUpdateForm)

    override fun getReportById(uttaksdataID: Int): Either<ServiceError, Report> = ReportRepository.getReportByID(uttaksdataID)

    override fun getReports(uttaksdataGetForm: ReportGetForm): Either<ServiceError, List<Report>> =
        ReportRepository.getReports(uttaksdataGetForm)


}