package no.oslokommune.ombruk.partner.database

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.partner.form.PartnerGetForm
import no.oslokommune.ombruk.partner.form.PartnerPostForm
import no.oslokommune.ombruk.partner.form.PartnerUpdateForm
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.shared.database.IRepository
import no.oslokommune.ombruk.shared.database.IRepositoryUniqueName
import no.oslokommune.ombruk.shared.error.RepositoryError

interface IPartnerRepository : IRepository, IRepositoryUniqueName {

    /**
     * Inserts a [Partner] into the database. The ID passed in the [PartnerPostForm] will be overriden, and a serial
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
     * @return An [Either] object consisting of a [RepositoryError] on failure and the updated [Partner] on success.
     */
    fun updatePartner(partner: PartnerUpdateForm): Either<RepositoryError, Partner>

    /**
     * Deletes a partner from the database with the specified partnerID.
     *
     * @param partnerID The ID of the partner to be deleted.
     * @return An [Either] object consisting of a [RepositoryError] on failure and [Unit] on success.
     */
    fun deletePartner(partnerID: Int): Either<RepositoryError, Unit>

    /**
     * Fetches a specific partner by its ID.
     *
     * @param partnerID The partner that should be fetched.
     * @return An [Either] object consisting of a [RepositoryError] on failure and a [Partner] on success.
     */
    fun getPartnerByID(partnerID: Int): Either<RepositoryError, Partner>

    /**
     * Fetches all partners. Supports getting partner by name.
     *
     * @return An [Either] object consisting of a [RepositoryError] on failure and a [List] of [Partner] objects on success.
     */
    @KtorExperimentalLocationsAPI
    fun getPartners(partnerGetForm: PartnerGetForm): Either<RepositoryError, List<Partner>>
}