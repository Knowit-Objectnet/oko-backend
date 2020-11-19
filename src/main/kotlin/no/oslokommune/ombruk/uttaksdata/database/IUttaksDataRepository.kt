package no.oslokommune.ombruk.uttaksdata.database

import arrow.core.Either
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.Uttaksdata
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataPostForm

interface IUttaksDataRepository {

    /**
     * Inserts a uttaksdata into the database. This function is automatically called when an uttak is saved, and should not
     * be used under any other circumstances. Not available through API.
     *
     * @param uttaksdataPostForm An [Uttak] object that has been stored to the database.
     * @return A [RepositoryError] on failure and a [Uttaksdata] on success.
     */
    fun insertUttaksdata(uttaksdataPostForm: UttaksdataPostForm): Either<RepositoryError, Uttaksdata>

    /**
     * Updates the weight of a specific uttaksdata. This function is intended for Partner uses, and only allows for the
     * weight of a uttaksdata to be updated.
     *
     * @param uttaksdataUpdateForm A [UttaksdataUpdateForm] where [UttaksdataUpdateForm.id] corresponds to a stored [Uttaksdata]
     * @return A [RepositoryError] on failure and an updated [Uttaksdata] on success.
     */
    fun updateUttaksdata(uttaksdataUpdateForm: UttaksdataUpdateForm): Either<RepositoryError, Uttaksdata>

    /**
     * Gets a specific [Uttaksdata] by its [Uttaksdata.id]. Must exist in the database.
     *
     * @param uttaksdataID An ID that corresponds to a [Uttaksdata.id].
     * @return A [RepositoryError] on failure and a [Uttaksdata] on success.
     */
    fun getUttaksDataByID(uttaksdataID: Int): Either<RepositoryError, Uttaksdata>

    fun getUttakByUttaksDataID(uttaksdataID: Int): Either<RepositoryError, Uttak>

    /**
     * Gets a [List] of [Uttaksdata] objects specified by parameters in a [UttaksdataGetForm].
     *
     * @param uttaksdataGetForm A [UttaksdataGetForm]. If null, all uttak are fetched from the DB.
     * @return A [RepositoryError] on failure and a [List] of [Uttaksdata] objects on success.
     */
    fun getUttaksData(uttaksdataGetForm: UttaksdataGetForm?): Either<RepositoryError, List<Uttaksdata>>

    fun deleteByUttakId(uttakId: Int): Either<ServiceError, Unit>
}