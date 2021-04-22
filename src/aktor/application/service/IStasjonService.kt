package ombruk.backend.aktor.application.service

import arrow.core.Either
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.shared.error.ServiceError

interface IStasjonService {
    /**
     * Saves a [Stasjon] to both the database and to Keycloak. If keycloak saving fails, the database transaction
     * rolls back.
     *
     * @param dto A custom object used to edit existing stasjoner. [dto] id must belong to an existing user.
     * @return An [Either] object consisting of [ServiceError] on failure or the ID of the saved stasjon on success.
     */
    fun save(dto: StasjonCreateDto): Either<ServiceError, Stasjon>

    /**
     * Gets a stasjon by its ID.
     *
     * @param id The ID of the user to get.
     * @return An [Either] object consisting of a [ServiceError] on failure or the requested [Stasjon] on success.
     */
    fun findOne(id: Int): Either<ServiceError, Stasjon>

    /**
     * Fetches stasjoner constrained by non-null values in the [StasjonFindDto].
     *
     * @param dto A [StasjonFindDto], where each non-null property will constrain the search.
     * @return An [Either] object consisting of [ServiceError] on failure or a [List] of [Stasjon] objects on success.
     */
    fun find(dto: StasjonFindDto = StasjonFindDto()): Either<ServiceError, List<Stasjon>>

    /**
     * Deletes the stasjon with the provided ID. If the ID does not exist, a [ServiceError] is returned.
     * @param id The ID of the user that should be deleted.
     * @return A [ServiceError] on failure and a [Stasjon] on success.
     */
    fun delete(id: Int): Either<ServiceError, Stasjon>

    /**
     * Updates a stasjon through the use of a stasjon dto (update object). The provided ID must correspond with
     * an existing user ID. If it does not, a [ServiceError] will be returned.
     *
     * @param dto The information that should be updated. ID cannot be updated and must correspond to an existing user.
     * @return A [ServiceError] on failure and the updated [Stasjon] on success.
     */
    fun update(dto: StasjonUpdateDto): Either<ServiceError, Stasjon>
}