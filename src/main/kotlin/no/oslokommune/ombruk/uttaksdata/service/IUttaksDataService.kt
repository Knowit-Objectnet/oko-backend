package no.oslokommune.ombruk.uttaksdata.service

import arrow.core.Either
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataGetForm
import no.oslokommune.ombruk.uttaksdata.model.UttaksData
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.shared.swagger.annotations.DefaultResponse
import no.oslokommune.ombruk.shared.swagger.annotations.ParameterFile
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataGetByIdForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataUpdateForm
import javax.ws.rs.*

@Path("/uttaksdata")
interface IUttaksDataService {

    fun saveUttaksData(uttak: Uttak): Either<ServiceError, UttaksData>

    /**
     * Updates a stored [UttaksData]. Only available internally, and is automatically called when an [Uttak] is updated.
     *
     * @param uttaksData A stored [Uttak] object.
     * @return A [ServiceError] on failure and a [Unit] on success.
     */
    @PATCH
    @DefaultResponse(
        okResponseBody = UttaksData::class,
        okResponseDescription = "Uttaksdata updated successfully",
        additionalResponses = [401, 403]
    )
    @Operation(
        summary = "Updates the Uttaksdata of an Uttak",
        security = [SecurityRequirement(name = "security")],
        tags = ["uttaksdata"],
        requestBody = RequestBody(
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UttaksDataUpdateForm::class)
            )]
        )
    )
    fun updateUttaksData(uttaksData: UttaksDataUpdateForm): Either<ServiceError, UttaksData>

    /**
     * Gets a [UttaksData] that corresponds with the specified [uttaksdataID].
     *
     * @param uttaksdataID ID of a stored [UttaksData]
     * @return A [ServiceError] on failure and a [UttaksData] on success.
     */
    @GET
    @Path("/{id}")
    @ParameterFile(UttaksDataGetByIdForm::class)
    @DefaultResponse(
        okResponseBody = UttaksData::class,
        okResponseDescription = "Uttaksdata was found",
        additionalResponses = [404]
    )
    @Operation(summary = "Fetches Uttaksdata for a specific uttak", tags = ["uttaksdata"])
    fun getUttaksDataById(@Parameter(hidden = true) uttaksdataID: Int): Either<ServiceError, UttaksData>

    /**
     * Gets a list of [UttaksData] objects specified by parameters in a [UttaksDataGetForm]
     *
     * @param uttaksDataGetForm a [UttaksDataGetForm] with values used to filter results. Null values are not used for filtering.
     * @return A [ServiceError] on failure and a [List] of [UttaksData] objects on success.
     */
    @GET
    @ParameterFile(UttaksDataGetForm::class)
    @DefaultResponse(
        okResponseBody = UttaksData::class,
        okResponseDescription = "Uttaksdata was found",
        okArrayResponse = true
    )
    @Operation(summary = "Fetches Uttaksdata, filtered by the parameters", tags = ["uttaksdata"])
    fun getUttaksData(@Parameter(hidden = true) uttaksDataGetForm: UttaksDataGetForm): Either<ServiceError, List<UttaksData>>

}