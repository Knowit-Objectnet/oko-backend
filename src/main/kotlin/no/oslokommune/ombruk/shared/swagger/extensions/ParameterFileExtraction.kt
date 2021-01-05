package no.oslokommune.ombruk.shared.swagger.extensions

import com.fasterxml.jackson.annotation.JsonView
import io.ktor.http.HttpStatusCode
import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.core.util.ReflectionUtils
import io.swagger.v3.jaxrs2.ext.AbstractOpenAPIExtension
import io.swagger.v3.jaxrs2.ext.OpenAPIExtension
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
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
//            temp.allowEmptyValue = it.allowEmptyValue
//            temp.deprecated = it.deprecated
//            temp.`$ref` = it.ref
//            temp.example = it.example
            temp.`in` = it.`in`.name
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

    private fun addResponses(operation: Operation, annotation: DefaultResponse) {
        val defaultResponses = listOf<HttpStatusCode>(
            HttpStatusCode.BadRequest,
            HttpStatusCode.Unauthorized,
            HttpStatusCode.NotFound,
            HttpStatusCode.Forbidden,
            HttpStatusCode.Conflict,
            HttpStatusCode.UnprocessableEntity,
            HttpStatusCode.InternalServerError
        )

        operation.responses.default = null
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
        defaultResponses.forEach {
            operation.responses.addApiResponse(
                it.value.toString(),
                ApiResponse().apply { description = it.description })
        }
        annotation.additionalResponses.forEach {
            operation.responses.addApiResponse(
                it.responseCode,
                ApiResponse().apply { description = it.description }
            )
        }
    }

}