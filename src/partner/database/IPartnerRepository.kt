package ombruk.backend.partner.database

import arrow.core.Either
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.partner.form.PartnerPostForm
import ombruk.backend.partner.form.PartnerUpdateForm
import ombruk.backend.partner.model.Partner

interface IPartnerRepository {

    /**
     * Inserts a [PartnerPostForm] into the database. The ID passed in the [PartnerPostForm] will be overriden, and a serial
     * ID will be used instead.
     *
     * @param partner A [PartnerPostForm]
     * @return An [Either] object consisting of a [RepositoryError] on failure and the ID of the saved partner on success.
     */
    fun insertPartner(partner: PartnerPostForm): Either<RepositoryError, Partner>

    /**
     * Updates a stored partner. The id passed in the [PartnerPostForm] must already exist in the database.
     *
     * @param partner A [PartnerPostForm] object containing the information that should be updated. ID cannot be altered.
     * @return An [Either] object consisting of a [RepositoryError] on failure and [Unit] on success.
     */
    fun updatePartner(partner: PartnerUpdateForm): Either<RepositoryError, Unit>

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