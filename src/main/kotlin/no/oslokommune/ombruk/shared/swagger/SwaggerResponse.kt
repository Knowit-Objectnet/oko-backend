package no.oslokommune.ombruk.shared.swagger

import io.ktor.http.HttpStatusCode

data class SwaggerResponse(val statusCode: HttpStatusCode, val description: String) {

    companion object {
        val BadRequest = SwaggerResponse(HttpStatusCode.BadRequest, "Bad Request")
        val Unauthorized = SwaggerResponse(HttpStatusCode.Unauthorized, "Unauthorized")
        val Forbidden = SwaggerResponse(HttpStatusCode.Forbidden, "Forbidden")
        val NotFound = SwaggerResponse(HttpStatusCode.NotFound, "Not found")
        val Conflict = SwaggerResponse(HttpStatusCode.Conflict, "Conflict")
        val Unprocessable = SwaggerResponse(HttpStatusCode.UnprocessableEntity, "Unprocessable entity")
        val InternalServerError = SwaggerResponse(HttpStatusCode.InternalServerError, "Internal Server Error")
    }
}