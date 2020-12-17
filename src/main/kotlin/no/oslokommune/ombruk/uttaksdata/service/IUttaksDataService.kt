package no.oslokommune.ombruk.uttaksdata.service

import arrow.core.Either
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetForm
import no.oslokommune.ombruk.uttaksdata.model.Uttaksdata
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.shared.swagger.annotations.DefaultResponse
import no.oslokommune.ombruk.shared.swagger.annotations.ParameterFile
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetByIdForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataPostForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataUpdateForm
import javax.ws.rs.*

@Path("/uttaksdata")
interface IUttaksDataService {
    /**
     * Saves a [Uttaksdata] to the database. Only available internally, and is automatically called when an [Uttak] is created.
     *
     * @param uttaksdataPostForm A stored [Uttak] object
     * @return A [ServiceError] on failure and a [Uttaksdata] on success.
     */
//    @POST
//    @DefaultResponse(okResponseBody = Uttaksdata::class, okResponseDescription = "Uttaksdata added successfully")
//    @Operation(
//        summary = "Adds Uttaksdata to a uttak",
//        tags = ["uttaksdata"],
//        requestBody = RequestBody(
//            content = [Content(
//                mediaType = "application/json",
//                schema = Schema(implementation = UttaksdataPostForm::class)
//            )]
//        )
//    )
    fun saveUttaksdata(uttaksdataPostForm: UttaksdataPostForm): Either<ServiceError, Uttaksdata>

    fun saveUttaksData(uttak: Uttak): Either<ServiceError, Uttaksdata>

    /**
     * Updates a stored [Uttaksdata]. Only available internally, and is automatically called when an [Uttak] is updated.
     *
     * @param uttaksdata A stored [Uttak] object.
     * @return A [ServiceError] on failure and a [Unit] on success.
     */
    @PATCH
    @DefaultResponse(okResponseBody = Uttaksdata::class, okResponseDescription = "Uttaksdata updated successfully")
    @Operation(
        summary = "Updates the Uttaksdata of an Uttak",
        tags = ["uttaksdata"],
        requestBody = RequestBody(
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UttaksdataUpdateForm::class)
            )]
        )
    )
    fun updateUttaksdata(uttaksdata: UttaksdataUpdateForm): Either<ServiceError, Uttaksdata>

    /**
     * Gets a [Uttaksdata] that corresponds with the specified [uttaksdataID].
     *
     * @param uttaksdataID ID of a stored [Uttaksdata]
     * @return A [ServiceError] on failure and a [Uttaksdata] on success.
     */
    @GET
    @Path("/{id}")
    @ParameterFile(UttaksdataGetByIdForm::class)
    @DefaultResponse(okResponseBody = Uttaksdata::class, okResponseDescription = "Uttaksdata was found")
    @Operation(summary = "Fetches Uttaksdata for a specific uttak", tags = ["uttaksdata"])
    fun getUttaksdataById(@Parameter(hidden = true) uttaksdataID: Int): Either<ServiceError, Uttaksdata>

    /**
     * Gets a list of [Uttaksdata] objects specified by parameters in a [UttaksdataGetForm]
     *
     * @param uttaksdataGetForm a [UttaksdataGetForm] with values used to filter results. Null values are not used for filtering.
     * @return A [ServiceError] on failure and a [List] of [Uttaksdata] objects on success.
     */
    @GET
    @ParameterFile(UttaksdataGetForm::class)
    @DefaultResponse(okResponseBody = Uttaksdata::class, okResponseDescription = "Uttaksdata was found")
    @Operation(summary = "Fetches Uttaksdata, filtered by the parameters", tags = ["uttaksdata"])
    fun getUttaksdata(@Parameter(hidden = true) uttaksdataGetForm: UttaksdataGetForm): Either<ServiceError, List<Uttaksdata>>
//
//    @DELETE
//    @Path("/{id}")
//    @DefaultResponse(okResponseBody = Unit::class, okResponseDescription = "Uttaksdata deleted successfully")
////    @PathParam("id")
//    @Operation(summary = "Deletes Uttaksdata based on its correspond UttaksID", tags = ["uttaksdata"])
    fun deleteByUttakId(@Parameter(`in` = ParameterIn.PATH, name = "id") uttakId: Int): Either<ServiceError, Unit>
}