package no.oslokommune.ombruk.uttak.service

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.shared.swagger.annotations.DefaultResponse
import no.oslokommune.ombruk.shared.swagger.annotations.ParameterFile
import no.oslokommune.ombruk.uttak.form.*
import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import javax.ws.rs.*

@Path("/uttak")
interface IUttakService {
    /**
     * Saves one or several uttak and automatically generates corresponding uttaksdata.
     * @param uttakPostForm An [UttakPostForm] that describes the uttak to be posted.
     * @return A [ServiceError] on failure and an [Uttak] on success. If saved uttak is recurring, the first uttak
     * is returned.
     */
    @POST
    @DefaultResponse(Uttak::class, "Uttak was successfully saved", additionalResponses = [401, 403])
    @Operation(
        summary = "Creates one or several new Uttak",
        description = "Must be admin",
        security = [SecurityRequirement(name = "security")],
        tags = ["uttak"],
        requestBody = RequestBody(
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UttakPostForm::class)
            )]
        )
    )
    fun saveUttak(uttakPostForm: UttakPostForm): Either<ServiceError, Uttak>

    /**
     * Gets a specific uttak by it's [Uttak.id].
     * @param id The id of the [Uttak] to get. Must exist in db.
     * @return A [ServiceError] on failure and the corresponding [Uttak] on success.
     */
    @GET
    @Path("/{id}")
    @ParameterFile(UttakGetByIdForm::class)
    @DefaultResponse(
        okResponseBody = Uttak::class,
        okResponseDescription = "Uttak was found",
        additionalResponses = [404]
    )
    @Operation(summary = "Get an Uttak by its ID", tags = ["uttak"])
    fun getUttakByID(@Parameter(hidden = true) id: Int): Either<ServiceError, Uttak>

    /**
     * Gets a list of uttak constrained by the values passed into the [uttakGetForm].
     * @param uttakGetForm The constraints to apply to the query. If all properties are null, all uttak will be queried.
     * @return A [ServiceError] on failure and a [List] of [Uttak] objects on success.
     */
    @KtorExperimentalLocationsAPI
    @GET
    @ParameterFile(UttakGetForm::class)
    @DefaultResponse(okResponseBody = Uttak::class, okResponseDescription = "Uttak was found", okArrayResponse = true)
    @Operation(summary = "Get a list of Uttak, filtered by parameters", tags = ["uttak"])
    fun getUttak(
        @Parameter(hidden = true) uttakGetForm: UttakGetForm? = null
    ): Either<ServiceError, List<Uttak>>

    /**
     * Deletes one or more uttak specified by the values passed into the [uttakDeleteForm].
     * @param uttakDeleteForm The constraints to apply to the query. If all properties are null, all uttak will be deleted.
     * @return A [ServiceError] on failure and a [List] of the deleted [Uttak] objects on success.
     */
    @KtorExperimentalLocationsAPI
    @DELETE
    @ParameterFile(UttakDeleteForm::class)
    @DefaultResponse(
        okResponseBody = Uttak::class,
        okResponseDescription = "Uttak deleted",
        okArrayResponse = true,
        additionalResponses = [401, 403]
    )
    @Operation(
        summary = "Deletes a list of Uttak, specified by the passed in parameters",
        security = [SecurityRequirement(name = "security")],
        tags = ["uttak"]
    )
    fun deleteUttak(uttak: UttakDeleteForm): Either<ServiceError, List<Uttak>>

//    fun deleteUttakById(@Parameter(hidden = true) id: Int): Either<ServiceError, Unit>

    /**
     * Updates a singular uttak. Must be called several times to update all uttak belonging to a recurrence rule.
     * @param uttakUpdate A [UttakUpdateForm] containing the values to be updated. Only non-null values will be updated.
     * @return A [ServiceError] on failure and the updated [Uttak] on success.
     */
    @PATCH
    @DefaultResponse(
        okResponseBody = Uttak::class,
        okResponseDescription = "Uttak updated",
        additionalResponses = [401, 403, 404]
    )
    @Operation(
        summary = "Updates an uttak",
        description = "Must be Station Worker or Admin",
        security = [SecurityRequirement(name = "security")],
        tags = ["uttak"],
        requestBody = RequestBody(
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UttakUpdateForm::class)
            )]
        )
    )
    fun updateUttak(uttakUpdate: UttakUpdateForm): Either<ServiceError, Uttak>
}
