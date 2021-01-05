package no.oslokommune.ombruk.stasjon.service

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.shared.swagger.annotations.DefaultResponse
import no.oslokommune.ombruk.shared.swagger.annotations.ParameterFile
import no.oslokommune.ombruk.stasjon.form.*
import javax.ws.rs.*

@Path("/stasjoner")
interface IStasjonService {

    /**
     * Get a single stasjon by its id.
     * @return Either a [ServiceError] or a [Stasjon].
     */
    @GET
    @Path("/{id}")
    @ParameterFile(StasjonGetByIdForm::class)
    @DefaultResponse(okResponseBody = Stasjon::class, okResponseDescription = "Stasjon was found")
    @Operation(
        summary = "Get a stasjon by its ID",
        tags = ["stasjoner"]
    )
    fun getStasjonById(@Parameter(hidden = true) id: Int): Either<ServiceError, Stasjon>

    /**
     * Gets all stasjoner
     * @return Either a [ServiceError] or a [List] of [Stasjon] objects. The list may be empty if there are no stasjoner
     */
    @KtorExperimentalLocationsAPI
    @GET
    @Path("/")
    @Operation(summary = "Gets all Stasjon objects that fulfill the constraints", tags = ["stasjoner"])
    @ParameterFile(StasjonGetForm::class)
    @DefaultResponse(
        okResponseBody = Stasjon::class,
        okResponseDescription = "Results matching query",
        okArrayResponse = true
    )
    fun getStasjoner(@Parameter(hidden = true) stasjonGetForm: StasjonGetForm): Either<ServiceError, List<Stasjon>>

    /**
     * Saves a stasjon.
     * @param stasjonPostForm Stasjon to save
     * @return Either a [ServiceError] or the saved [Stasjon]
     */
    @POST
    @DefaultResponse(okResponseBody = Stasjon::class, okResponseDescription = "Stasjon was created")
    @Operation(
        summary = "Create a new station",
        tags = ["stasjoner"],
        requestBody = RequestBody(
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = StasjonPostForm::class)
            )]
        )
    )
    fun saveStasjon(stasjonPostForm: StasjonPostForm): Either<ServiceError, Stasjon>

    /**
     * Update a stasjon.
     * @param stasjonUpdateForm Stasjon to update
     * @return Either a [ServiceError] or the updated [Stasjon]
     */
    @PATCH
    @DefaultResponse(okResponseBody = Stasjon::class, okResponseDescription = "The updated Stasjon")
    @Operation(
        summary = "Updates the station with the provided ID if it exists",
        tags = ["stasjoner"],
        requestBody = RequestBody(
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = StasjonUpdateForm::class)
            )]
        )
    )
    fun updateStasjon(stasjonUpdateForm: StasjonUpdateForm): Either<ServiceError, Stasjon>

    /**
     * Delete a stasjon.
     * @param id ID of stasjon to delete
     * @return A [ServiceError] on failure and the deleted [Stasjon] on success.
     */
    @DELETE
    @ParameterFile(StasjonDeleteForm::class)
    @DefaultResponse(okResponseBody = Stasjon::class, okResponseDescription = "The deleted Stasjon")
    @Operation(summary = "Deletes the user with the specified ID", tags = ["stasjoner"])
    fun deleteStasjonById(@Parameter(hidden = true) id: Int): Either<ServiceError, Stasjon>
}
