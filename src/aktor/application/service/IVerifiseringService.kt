package ombruk.backend.aktor.application.service

import ombruk.backend.shared.error.ServiceError


import arrow.core.Either
import io.ktor.locations.*
import ombruk.backend.aktor.application.api.dto.*
import ombruk.backend.aktor.domain.entity.Verifisering
import ombruk.backend.aktor.domain.entity.Verifisert
import ombruk.backend.aktor.domain.model.VerifiseringUpdateParams
import java.util.*

interface IVerifiseringService {

    /**
     * Saves a [Kontakt] to both the database and to Keycloak. If keycloak saving fails, the database transaction
     * rolls back.
     *
     * @param dto A custom object used to edit existing kontakter. [dto] id must belong to an existing user.
     * @return An [Either] object consisting of [ServiceError] on failure or the ID of the saved kontakt on success.
     */
    fun save(dto: VerifiseringSaveDto): Either<ServiceError, Verifisering>

    /**
     * Gets a Kontakt by its ID.
     *
     * @param id The ID of the user to get.
     * @return An [Either] object consisting of a [ServiceError] on failure or the requested [Kontakt] on success.
     */
    fun getVerifiseringById(id: UUID): Either<ServiceError, Verifisering>

    /**
     * Fetches kontaker constrained by non-null values in the [KontaktGetDto].
     *
     * @param dto A [KontaktGetDto], where each non-null property will constrain the search.
     * @return An [Either] object consisting of [ServiceError] on failure or a [List] of [Kontakt] objects on success.
     */
    //fun getKontakter(dto: KontaktGetDto = KontaktGetDto()): Either<ServiceError, List<Kontakt>>

    @KtorExperimentalLocationsAPI
    fun verifiser(dto: KontaktVerifiseringDto): Either<ServiceError, Verifisert>

    /**
     * Deletes the Kontakt with the provided ID. If the ID does not exist, a [ServiceError] is returned.
     * @param id The ID of the user that should be deleted.
     * @return A [ServiceError] on failure and a [Kontakt] on success.
     */
    fun deleteVerifiseringById(id: UUID): Either<ServiceError, Verifisering>

    /**
     * Updates a Kontakt through the use of a Kontakt dto (update object). The provided ID must correspond with
     * an existing user ID. If it does not, a [ServiceError] will be returned.
     *
     * @param dto The information that should be updated. ID cannot be updated and must correspond to an existing user.
     * @return A [ServiceError] on failure and the updated [Kontakt] on success.
     */
    fun update(dto: VerifiseringUpdateDto): Either<ServiceError, Verifisering>
}
