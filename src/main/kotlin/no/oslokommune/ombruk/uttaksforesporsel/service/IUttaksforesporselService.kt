package no.oslokommune.ombruk.uttaksforesporsel.service

import arrow.core.Either
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselDeleteForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksForesporselGetForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselPostForm
import no.oslokommune.ombruk.uttaksforesporsel.model.UttaksForesporsel
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.shared.swagger.annotations.DefaultResponse
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/uttaksforesporsel")
interface IUttaksforesporselService {

    /**
     * Adds a uttaksforesporsel to a [Pickup].
     *
     * @param requestPostForm A [UttaksforesporselPostForm] that specifies what partner should be added to what uttaksforesporsel.
     * @return A [ServiceError] on failure and the stored [UttaksForesporsel] on success.
     */
    @POST
    @DefaultResponse(okResponseBody = UttaksForesporsel::class, okResponseDescription = "UttaksForesporsel created")
    @Operation(
        summary = "Creates a new UttaksForesporsel",
        tags = ["uttaksforesporsel"],
        requestBody = RequestBody(
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UttaksforesporselPostForm::class)
            )]
        )
    )
    fun saveRequest(requestPostForm: UttaksforesporselPostForm): Either<ServiceError, UttaksForesporsel>

    /**
     * Gets a [List] of [UttaksForesporsel] objects that can be filtered with constraints. Seeing as a [UttaksForesporsel] has no primary key,
     * one has to GET a specific [UttaksForesporsel] by specifying both a partner id and a no.oslokommune.ombruk.pickup id.
     *
     * @param requestGetForm a [UttaksForesporselGetForm] with constraints that are only added if they are not null.
     * @return A [ServiceError] on success and a [List] of [UttaksForesporsel] objects on success.
     */
    fun getRequests(requestGetForm: UttaksForesporselGetForm? = null): Either<ServiceError, List<UttaksForesporsel>>

    /**
     * Deletes a uttaksforesporsel from a [Pickup]
     *
     * @param requestDeleteForm A [UttaksforesporselDeleteForm] that specifies what [UttaksForesporsel] should be deleted.
     * @return A [ServiceError] on failure and an [Int] specifying how many [UttaksForesporsel] objects were deleted on success.
     */
    fun deleteRequest(requestDeleteForm: UttaksforesporselDeleteForm): Either<ServiceError, Int>
}