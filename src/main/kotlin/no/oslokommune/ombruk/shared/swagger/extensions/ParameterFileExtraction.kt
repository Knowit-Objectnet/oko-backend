package no.oslokommune.ombruk.shared.swagger.extensions

import io.ktor.http.HttpStatusCode
import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.jaxrs2.ext.AbstractOpenAPIExtension
import io.swagger.v3.jaxrs2.ext.OpenAPIExtension
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import no.oslokommune.ombruk.shared.swagger.SwaggerResponse
import no.oslokommune.ombruk.shared.swagger.annotations.DefaultResponse
import no.oslokommune.ombruk.shared.swagger.annotations.ParameterFile
import java.lang.reflect.Method
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

class ParameterFileExtraction(val openAPI: OpenAPI) : AbstractOpenAPIExtension() {

    override fun decorateOperation(operation: Operation, method: Method, chain: MutableIterator<OpenAPIExtension>?) {
        val annotationList = method.annotations as Array<Annotation>
        annotationList.forEach { annotation ->
            when (annotation) {
                is ParameterFile -> addParameters(operation, annotation)
                is DefaultResponse -> addResponses(operation, annotation)
            }
        }
    }

    private fun addParameters(operation: Operation, annotation: ParameterFile) {
        val params = annotation.parameters.memberProperties.flatMap {
            it.javaGetter?.annotations?.filter { it.annotationClass == Parameter::class }?.map { it as Parameter }
                ?: emptyList()
        }
        operation.parameters = params.map {
            val temp = io.swagger.v3.oas.models.parameters.Parameter()
            temp.`in` = it.`in`.name.toLowerCase()
            temp.description = it.description
            temp.required = it.required
            temp.name = it.name
            val schema: Schema<Any> = Schema()
            schema.format = it.schema.format
            schema.type = it.schema.type
            temp.schema = schema
            temp
        }

    }

    private fun generateSchema(annotation: DefaultResponse): Schema<*> {
        val schema = AnnotationsUtils.resolveSchemaFromType(
            annotation.okResponseBody.java,
            openAPI.components,
            null
        )
        return if (annotation.okArrayResponse) ArraySchema().items(schema) else schema
    }

    private fun addResponse(operation: Operation, response: SwaggerResponse) {
        operation.responses.addApiResponse(
            response.statusCode.value.toString(),
            ApiResponse().apply { description = response.description }
        )
    }

    private fun responseFromStatusCode(statusCode: Int): SwaggerResponse {
        return when (statusCode) {
            HttpStatusCode.Unauthorized.value -> SwaggerResponse.Unauthorized
            HttpStatusCode.Forbidden.value -> SwaggerResponse.Forbidden
            HttpStatusCode.NotFound.value -> SwaggerResponse.NotFound
            HttpStatusCode.Conflict.value -> SwaggerResponse.Conflict
            else -> throw Exception("Invalid status code provided")
        }
    }

    private fun add200Response(operation: Operation, annotation: DefaultResponse) {
        operation.responses.addApiResponse(
            HttpStatusCode.OK.value.toString(),
            ApiResponse().apply {
                description = annotation.okResponseDescription
                content = Content().addMediaType(
                    "application/json",
                    MediaType().schema(
                        generateSchema(annotation)
                    )
                )
            }
        )
    }

    private fun addResponses(operation: Operation, annotation: DefaultResponse) {
        val defaultResponses = listOf(
            SwaggerResponse.BadRequest,
            SwaggerResponse.Unprocessable,
            SwaggerResponse.InternalServerError
        )

        operation.responses.default = null

        add200Response(operation, annotation)
        defaultResponses.forEach { addResponse(operation, it) }
        annotation.additionalResponses.map { responseFromStatusCode(it) }.forEach { addResponse(operation, it) }
    }

}