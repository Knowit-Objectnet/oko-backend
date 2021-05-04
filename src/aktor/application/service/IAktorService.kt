package ombruk.backend.aktor.application.service

import arrow.core.Either
import ombruk.backend.aktor.domain.enum.AktorType
import ombruk.backend.shared.error.ServiceError

interface IAktorService {
    /**
     * Gets a AktorType by its ID.
     *
     * @param id The ID of the user to get.
     * @return An [Either] object consisting of a [ServiceError] on failure or the requested [AktorType] on success.
     */
    fun findOne(id: Int): Either<ServiceError, AktorType>
}