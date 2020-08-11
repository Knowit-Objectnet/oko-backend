package ombruk.backend.partner.service

import arrow.core.Either
import ombruk.backend.partner.form.PartnerGetForm
import ombruk.backend.partner.form.PartnerPostForm
import ombruk.backend.partner.form.PartnerUpdateForm
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.error.ServiceError

interface IPartnerService {

    /**
     * Saves a [Partner] to both the database and to Keycloak. If keycloak saving fails, the database transaction
     * rolls back.
     *
     * @param partnerForm A custom object used to edit existing partners. [partnerForm] id must belong to an existing user.
     * @return An [Either] object consisting of [ServiceError] on failure or the ID of the saved partner on success.
     */
    fun savePartner(partnerForm: PartnerPostForm): Either<ServiceError, Partner>

    /**
     * Gets a partner by its ID.´
     *
     * @param id The ID of the user to get.
     * @return An [Either] object consisting of a [ServiceError] on failure or the requested [Partner] on success.
     */
    fun getPartnerById(id: Int): Either<ServiceError, Partner>

    /**
     * Fetches partners constrained by non-null values in the [PartnerGetForm].
     *
     * @param partnerGetForm A [PartnerGetForm], where each non-null property will constrain the search.
     * @return An [Either] object consisting of [ServiceError] on failure or a [List] of [Partner] objects on success.
     */
    fun getPartners(partnerGetForm: PartnerGetForm): Either<ServiceError, List<Partner>>

    /**
     * Deletes the partner with the provided ID. If the ID does not exist, a [ServiceError] is returned.
     * @param id The ID of the user that should be deleted.
     * @return A [ServiceError] on failure and a [Partner] on success.
     */
    fun deletePartnerById(id: Int): Either<ServiceError, Partner>

    /**
     * Updates a partner through the use of a partner form (update object). The provided ID must correspond with
     * an existing user ID. If it does not, a [ServiceError] will be returned.
     *
     * @param partnerForm The information that should be updated. ID cannot be updated and must correspond to an existing user.
     * @return A [ServiceError] on failure and the updated [Partner] on success.
     */
    fun updatePartner(partnerForm: PartnerUpdateForm): Either<ServiceError, Partner>
}
