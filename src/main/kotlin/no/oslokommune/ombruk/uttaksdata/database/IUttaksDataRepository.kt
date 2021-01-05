package no.oslokommune.ombruk.uttaksdata.database

import arrow.core.Either
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.UttaksData
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.shared.error.ServiceError
//import no.oslokommune.ombruk.uttaksdata.form.UttaksdataPostForm

interface IUttaksDataRepository {

//    /**
//     * Inserts a uttaksdata into the database. This function is automatically called when an uttak is saved, and should not
//     * be used under any other circumstances. Not available through API.
//     *
//     * @param uttaksdataPostForm An [Uttak] object that has been stored to the database.
//     * @return A [RepositoryError] on failure and a [Uttaksdata] on success.
//     */
//    fun insertUttaksdata(uttaksdataPostForm: UttaksdataPostForm): Either<RepositoryError, Uttaksdata>

    /**
     * Updates the weight of a specific uttaksdata. This function is intended for Partner uses, and only allows for the
     * weight of a uttaksdata to be updated.
     *
     * @param uttaksDataUpdateForm A [UttaksDataUpdateForm] where [UttaksDataUpdateForm.id] corresponds to a stored [UttaksData]
     * @return A [RepositoryError] on failure and an updated [UttaksData] on success.
     */
    fun updateUttaksData(uttaksDataUpdateForm: UttaksDataUpdateForm): Either<RepositoryError, UttaksData>

    /**
     * Gets a specific [UttaksData] by its [UttaksData.id]. Must exist in the database.
     *
     * @param uttaksdataID An ID that corresponds to a [UttaksData.id].
     * @return A [RepositoryError] on failure and a [UttaksData] on success.
     */
    fun getUttaksDataById(uttaksdataId: Int): Either<RepositoryError, UttaksData>

    /**
     * Gets a [List] of [UttaksData] objects specified by parameters in a [UttaksDataGetForm].
     *
     * @param uttaksDataGetForm A [UttaksDataGetForm]. If null, all uttak are fetched from the DB.
     * @return A [RepositoryError] on failure and a [List] of [UttaksData] objects on success.
     */
    fun getUttaksData(uttaksDataGetForm: UttaksDataGetForm?): Either<RepositoryError, List<UttaksData>>

    fun deleteByUttakId(uttakId: Int): Either<ServiceError, Unit>
}