package no.oslokommune.ombruk.shared.swagger.annotations

import io.ktor.http.HttpStatusCode
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kotlin.reflect.KClass

annotation class DefaultResponse(
    val okResponseBody: KClass<*>,
    val okResponseDescription: String,
    val additionalResponses: Array<ApiResponse> = []

)