package no.oslokommune.ombruk.shared.swagger.extensions

import io.ktor.http.HttpStatusCode
import io.swagger.v3.jaxrs2.ext.AbstractOpenAPIExtension
import io.swagger.v3.jaxrs2.ext.OpenAPIExtension
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import no.oslokommune.ombruk.shared.swagger.annotations.DefaultResponse
import no.oslokommune.ombruk.shared.swagger.annotations.ParameterFile
import java.lang.reflect.Method

class DefaultResponseExtension : AbstractOpenAPIExtension() {
    val defaultResponses = listOf<HttpStatusCode>(
        HttpStatusCode.BadRequest,
        HttpStatusCode.Unauthorized,
        HttpStatusCode.NotFound,
        HttpStatusCode.Forbidden,
        HttpStatusCode.Conflict,
        HttpStatusCode.UnprocessableEntity,
        HttpStatusCode.InternalServerError
    )

    override fun decorateOperation(operation: Operation, method: Method, chain: MutableIterator<OpenAPIExtension>?) {
        val annotationList = method.annotations as Array<Annotation>
        annotationList.forEach { annotation ->
            when (annotation) {
                is DefaultResponse -> addResponses(operation, annotation)
            }
        }
    }

    private fun addResponses(operation: Operation, annotation: DefaultResponse) {
//        operation.responses.addApiResponse(
//            HttpStatusCode.OK.value.toString(),
//            ApiResponse().apply {
//                val temp = Content().apply { addMediaType("application/json", MediaType().apply {
//                    val sch = Schema<Any>()
//                    sch.
//                }) }
////                temp.addMediaType("application/json", MediaType().schema())
//                content = Content().addMediaType("application/json", MediaType())
//            }
//        )
        defaultResponses.forEach {
            operation.responses.addApiResponse(
                it.value.toString(),
                ApiResponse().apply { description = it.description })
        }
//        annotation.additionalResponses.forEach {
//            operation.responses.addApiResponse(
//                it.responseCode,
//                ApiResponse().apply { description = it.description }
//            )
//        }
    }
}