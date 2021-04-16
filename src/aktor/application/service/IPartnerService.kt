package ombruk.backend.aktor.application.service

import ombruk.backend.aktor.application.api.dto.PartnerGetDto
import ombruk.backend.aktor.application.api.dto.PartnerPostDto
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.shared.error.ServiceError


import arrow.core.Either
import ombruk.backend.aktor.application.api.dto.PartnerUpdateDto

interface IPartnerService {

    /**
     * Saves a [Partner] to both the database and to Keycloak. If keycloak saving fails, the database transaction
     * rolls back.
     *
     * @param dto A custom object used to edit existing partnere. [dto] id must belong to an existing user.
     * @return An [Either] object consisting of [ServiceError] on failure or the ID of the saved partner on success.
     */
    fun savePartner(dto: PartnerPostDto): Either<ServiceError, Partner>

    /**
     * Gets a partner by its ID.
     *
     * @param id The ID of the user to get.
     * @return An [Either] object consisting of a [ServiceError] on failure or the requested [Partner] on success.
     */
    fun getPartnerById(id: Int): Either<ServiceError, Partner>

    /**
     * Fetches partnere constrained by non-null values in the [PartnerGetDto].
     *
     * @param dto A [PartnerGetDto], where each non-null property will constrain the search.
     * @return An [Either] object consisting of [ServiceError] on failure or a [List] of [Partner] objects on success.
     */
    fun getPartnere(dto: PartnerGetDto = PartnerGetDto()): Either<ServiceError, List<Partner>>

    /**
     * Deletes the partner with the provided ID. If the ID does not exist, a [ServiceError] is returned.
     * @param id The ID of the user that should be deleted.
     * @return A [ServiceError] on failure and a [Partner] on success.
     */
    fun deletePartnerById(id: Int): Either<ServiceError, Partner>

    /**
     * Updates a partner through the use of a partner dto (update object). The provided ID must correspond with
     * an existing user ID. If it does not, a [ServiceError] will be returned.
     *
     * @param dto The information that should be updated. ID cannot be updated and must correspond to an existing user.
     * @return A [ServiceError] on failure and the updated [Partner] on success.
     */
    fun updatePartner(dto: PartnerUpdateDto): Either<ServiceError, Partner>
}
