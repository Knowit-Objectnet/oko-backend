package no.oslokommune.ombruk.uttaksforesporsel.service

import arrow.core.Either
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselDeleteForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksForesporselGetForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselPostForm
import no.oslokommune.ombruk.uttaksforesporsel.model.UttaksForesporsel
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.shared.swagger.annotations.DefaultResponse
import no.oslokommune.ombruk.shared.swagger.annotations.ParameterFile
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import no.oslokommune.ombruk.uttak.model.Uttak;

@Path("/uttaksforesporsel")
interface IUttaksforesporselService {

    /**
     * Adds a uttaksforesporsel to a [Uttak].
     *
     * @param foresporselPostForm A [UttaksforesporselPostForm] that specifies what partner should be added to what uttaksforesporsel.
     * @return A [ServiceError] on failure and the stored [UttaksForesporsel] on success.
     */
    @POST
    @DefaultResponse(
        okResponseBody = UttaksForesporsel::class,
        okResponseDescription = "UttaksForesporsel created",
        additionalResponses = [401, 403]
    )
    @Operation(
        summary = "Creates a new UttaksForesporsel",
        description = "Must be partner",
        security = [SecurityRequirement(name = "security")],
        tags = ["uttaksforesporsel"],
        requestBody = RequestBody(
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UttaksforesporselPostForm::class)
            )]
        )
    )
    fun saveForesporsel(foresporselPostForm: UttaksforesporselPostForm): Either<ServiceError, UttaksForesporsel>

    /**
     * Gets a [List] of [UttaksForesporsel] objects that can be filtered with constraints. Seeing as a [UttaksForesporsel] has no primary key,
     * one has to GET a specific [UttaksForesporsel] by specifying both a partner id and a [Uttak.id]
     *
     * @param foresporselGetForm a [UttaksForesporselGetForm] with constraints that are only added if they are not null.
     * @return A [ServiceError] on success and a [List] of [UttaksForesporsel] objects on success.
     */
    @GET
    @Operation(summary = "Gets all foresporsler that fit the provided parameters", tags = ["uttaksforesporsel"])
    @ParameterFile(UttaksForesporselGetForm::class)
    @DefaultResponse(
        okResponseDescription = "Query Matches",
        okResponseBody = UttaksForesporsel::class,
        okArrayResponse = true
    )
    fun getForesporsler(@Parameter(hidden = true) foresporselGetForm: UttaksForesporselGetForm? = null): Either<ServiceError, List<UttaksForesporsel>>

    /**
     * Deletes a uttaksforesporsel from a [Uttak]
     *
     * @param foresporselDeleteForm A [UttaksforesporselDeleteForm] that specifies what [UttaksForesporsel] should be deleted.
     * @return A [ServiceError] on failure and an [Int] specifying how many [UttaksForesporsel] objects were deleted on success.
     */
    @DELETE
    @Operation(
        summary = "Deletes a Uttaksforesporsel",
        description = "Must be partner",
        security = [SecurityRequirement(name = "security")],
        tags = ["uttaksforesporsel"]
    )
    @ParameterFile(UttaksforesporselDeleteForm::class)
    @DefaultResponse(
        okResponseDescription = "The amount of deleted Uttaksforesporsel",
        okResponseBody = Int::class,
        additionalResponses = [401, 403]
    )
    fun deleteForesporsel(foresporselDeleteForm: UttaksforesporselDeleteForm): Either<ServiceError, Int>
}