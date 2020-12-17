package no.oslokommune.ombruk.shared.swagger

import com.fasterxml.jackson.databind.JavaType
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverter
import io.swagger.v3.core.converter.ModelConverterContext
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.media.DateSchema
import io.swagger.v3.oas.models.media.Schema
import java.time.LocalTime
import java.util.*

class LocalTimeConverter : ModelConverter {
    override fun resolve(
        type: AnnotatedType?,
        context: ModelConverterContext,
        chain: MutableIterator<ModelConverter>
    ): Schema<*>? {
        if(type == null) return null
        if(type.isSchemaProperty) {
            val _type: JavaType? = Json.mapper().constructType(type.type)
            if(_type != null) {
                val cls: Class<*> = _type.rawClass
                if(cls == java.time.LocalTime::class.java) {
                    val schema : Schema<Date> = DateSchema()
                    schema.format("partial-time")
                    return schema
                }
            }
        }
        return if(chain.hasNext()) {
            chain.next().resolve(type, context, chain)
        } else {
            null
        }
    }
}