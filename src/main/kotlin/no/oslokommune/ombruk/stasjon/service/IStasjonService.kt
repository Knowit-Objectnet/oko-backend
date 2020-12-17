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
    @Operation(
        summary = "Get a stasjon by its ID",
        tags = ["stasjoner"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Stasjon was found",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Stasjon::class)
                )]
            ),
            ApiResponse(responseCode = "404", description = "Stasjon could not be found"),
            ApiResponse(responseCode = "400", description = "Bad input parameter"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun getStasjonById(@Parameter(hidden = true) id: Int): Either<ServiceError, Stasjon>

    /**
     * Gets all stasjoner
     * @return Either a [ServiceError] or a [List] of [Stasjon] objects. The list may be empty if there are no stasjoner
     */
    @KtorExperimentalLocationsAPI
    @GET
    @Path("/")
    @ParameterFile(StasjonGetForm::class)
    @Operation(
        summary = "Gets all Stasjon objects that fulfill the constraints",
        tags = ["stasjoner"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Stasjon was found",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = Stasjon::class))
                )]
            ),
            ApiResponse(responseCode = "404", description = "Stasjon could not be found"),
            ApiResponse(responseCode = "400", description = "Bad input parameter"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun getStasjoner(@Parameter(hidden = true) stasjonGetForm: StasjonGetForm): Either<ServiceError, List<Stasjon>>

    /**
     * Saves a stasjon.
     * @param stasjonPostForm Stasjon to save
     * @return Either a [ServiceError] or the saved [Stasjon]
     */
    @POST
    @Operation(
        summary = "Create a new station",
        tags = ["stasjoner"],
        requestBody = RequestBody(
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = StasjonPostForm::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Stasjon was created",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Stasjon::class))]
            ),
            ApiResponse(responseCode = "400", description = "Bad input parameter"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Insufficient role privileges"),
            ApiResponse(responseCode = "409", description = "A stasjon with that name already exists"),
            ApiResponse(responseCode = "422", description = "Validation of stasjon body failed"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun saveStasjon(stasjonPostForm: StasjonPostForm): Either<ServiceError, Stasjon>

    /**
     * Update a stasjon.
     * @param stasjonUpdateForm Stasjon to update
     * @return Either a [ServiceError] or the updated [Stasjon]
     */
    @PATCH
    @Operation(
        summary = "Updates the station with the provided ID if it exists",
        tags = ["stasjoner"],
        requestBody = RequestBody(
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = StasjonUpdateForm::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Stasjon was updated",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Stasjon::class))]
            ),
            ApiResponse(responseCode = "400", description = "Bad input parameter"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Insufficient role privileges"),
            ApiResponse(responseCode = "422", description = "Validation of stasjon body failed"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun updateStasjon(stasjonUpdateForm: StasjonUpdateForm): Either<ServiceError, Stasjon>

    /**
     * Delete a stasjon.
     * @param id ID of stasjon to delete
     * @return A [ServiceError] on failure and the deleted [Stasjon] on success.
     */
    @DELETE
    @ParameterFile(StasjonDeleteForm::class)
    @Operation(
        summary = "Deletes the user with the specified ID",
        tags = ["stasjoner"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "The deleted Stasjon",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Stasjon::class))]
            ),
            ApiResponse(responseCode = "400", description = "Bad input parameter"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Insufficient role privileges"),
            ApiResponse(responseCode = "422", description = "Validation of parameter body failed"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun deleteStasjonById(@Parameter(hidden = true) id: Int): Either<ServiceError, Stasjon>
}
