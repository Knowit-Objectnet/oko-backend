package ombruk.backend.partner.database

import arrow.core.Either
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.partner.form.PartnerForm
import ombruk.backend.partner.model.Partner

interface IPartnerRepository {

    /**
     * Inserts a [PartnerForm] into the database. The ID passed in the [PartnerForm] will be overriden, and a serial
     * ID will be used instead.
     *
     * @param partner A [PartnerForm]
     * @return An [Either] object consisting of a [RepositoryError] on failure and the ID of the saved partner on success.
     */
    fun insertPartner(partner: PartnerForm): Either<RepositoryError, Partner>

    /**
     * Updates a stored partner. The id passed in the [PartnerForm] must already exist in the database.
     *
     * @param partner A [PartnerForm] object containing the information that should be updated. ID cannot be altered.
     * @return An [Either] object consisting of a [RepositoryError] on failure and [Unit] on success.
     */
    fun updatePartner(partner: PartnerForm): Either<RepositoryError, Unit>

    /**
     * Deletes a partner from the database with the specified partnerID.
     *
     * @param partnerID The ID of the partner to be deleted.
     * @return An [Either] object consisting of a [RepositoryError] on failure and [Unit] on success.
     */
    fun deletePartner(partnerID: Int): Either<RepositoryError, Unit>

    /**
     * Fetches a specific partner.
     *
     * @param partnerID The partner that should be fetched.
     * @return An [Either] object consisting of a [RepositoryError] on failure and a [Partner] on success.
     */
    fun getPartnerByID(partnerID: Int): Either<RepositoryError, Partner>

    /**
     * Fetches all partners.
     *
     * @return An [Either] object consisting of a [RepositoryError] on failure and a [List] of [Partner] objects on success.
     */
    fun getPartners(): Either<RepositoryError, List<Partner>>
}