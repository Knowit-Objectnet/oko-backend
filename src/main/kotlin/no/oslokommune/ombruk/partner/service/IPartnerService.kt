package no.oslokommune.ombruk.partner.service

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.OAuthFlow
import io.swagger.v3.oas.annotations.security.OAuthFlows
import io.swagger.v3.oas.annotations.security.OAuthScope
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.oslokommune.ombruk.partner.form.*
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.shared.api.KeycloakGroupIntegration
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.shared.swagger.annotations.DefaultResponse
import no.oslokommune.ombruk.shared.swagger.annotations.ParameterFile
import javax.ws.rs.*

@OpenAPIDefinition(
    info = Info(
        title = "OpenAPIDefinition annotation is required only once in a project",
        version = "1",
        description = "Pretty neat API docs for the OKO API"
    )
)
@Path("/partnere")
@SecurityScheme(
    name = "security",
    type = SecuritySchemeType.OAUTH2,
    `in` = SecuritySchemeIn.HEADER,
    flows = OAuthFlows(
        authorizationCode = OAuthFlow(
            authorizationUrl = "http://localhost",
            scopes = [OAuthScope(name = "openid")],
            tokenUrl = "http://",
            refreshUrl = "Http://"
        )
    )

)
//private val keycloakBaseUrl = KeycloakGroupIntegration.
//private val keycloakRealm = KeycloakGroupIntegration.appConfig.property("ktor.keycloak.keycloakRealm").getString()
//private val tokenUrl = keycloakBaseUrl + "realms/$keycloakRealm/protocol/openid-connect/token"

interface IPartnerService {

    /**
     * Saves a [Partner] to both the database and to Keycloak. If keycloak saving fails, the database transaction
     * rolls back.
     *
     * @param partnerForm A custom object used to edit existing partnere. [partnerForm] id must belong to an existing user.
     * @return An [Either] object consisting of [ServiceError] on failure or the ID of the saved partner on success.
     */
    @POST
    @DefaultResponse(Partner::class, "Partner was found")
    @Operation(
        summary = "Create a new Partner",
        tags = ["partner"],
        requestBody = RequestBody(
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = PartnerPostForm::class)
            )]
        )
    )
    fun savePartner(partnerForm: PartnerPostForm): Either<ServiceError, Partner>

    /**
     * Gets a partner by its ID.
     *
     * @param id The ID of the user to get.
     * @return An [Either] object consisting of a [ServiceError] on failure or the requested [Partner] on success.
     */
    @GET
    @Path("/{id}")
    @ParameterFile(PartnerGetByIdForm::class)
    @DefaultResponse(okResponseBody = Partner::class, okResponseDescription = "Partner was found")
    @Operation(summary = "Get a partner by its ID",tags = ["partner"])
    fun getPartnerById(@Parameter(hidden = true) id: Int): Either<ServiceError, Partner>

    /**
     * Fetches partnere constrained by non-null values in the [PartnerGetForm].
     *
     * @param partnerGetForm A [PartnerGetForm], where each non-null property will constrain the search.
     * @return An [Either] object consisting of [ServiceError] on failure or a [List] of [Partner] objects on success.
     */
    @GET
    @ParameterFile(PartnerGetForm::class)
    @DefaultResponse(Partner::class, "Partner was found", okArrayResponse = true)
    @Operation(summary = "Fetches partners, filtered by the passed parameters", tags = ["partner"])
    @JsonIgnore
    fun getPartnere(@BeanParam partnerGetForm: PartnerGetForm = PartnerGetForm()): Either<ServiceError, List<Partner>>

    /**
     * Deletes the partner with the provided ID. If the ID does not exist, a [ServiceError] is returned.
     * @param id The ID of the user that should be deleted.
     * @return A [ServiceError] on failure and a [Partner] on success.
     */
    @DELETE
    @Path("/{id}")
    @ParameterFile(PartnerDeleteForm::class)
    @DefaultResponse(okResponseBody = Partner::class, okResponseDescription = "The deleted partner")
    @Operation(summary = "Delete an existing partner", tags = ["partner"])
    fun deletePartnerById(@Parameter(hidden = true) id: Int): Either<ServiceError, Partner>

    /**
     * Updates a partner through the use of a partner form (update object). The provided ID must correspond with
     * an existing user ID. If it does not, a [ServiceError] will be returned.
     *
     * @param partnerForm The information that should be updated. ID cannot be updated and must correspond to an existing user.
     * @return A [ServiceError] on failure and the updated [Partner] on success.
     */
    @PATCH
    @DefaultResponse(okResponseBody = Partner::class, okResponseDescription = "The updated partner")
    @Operation(
        summary = "Update an existing partner",
        tags = ["partner"],
        requestBody = RequestBody(
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = PartnerUpdateForm::class)
            )]
        )
    )
    fun updatePartner(partnerForm: PartnerUpdateForm): Either<ServiceError, Partner>
}
