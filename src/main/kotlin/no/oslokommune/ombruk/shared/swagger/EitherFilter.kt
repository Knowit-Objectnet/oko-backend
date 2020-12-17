package no.oslokommune.ombruk.shared.swagger

import io.swagger.v3.core.filter.AbstractSpecFilter
import io.swagger.v3.core.model.ApiDescription
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.Schema
import java.util.*

class EitherFilter : AbstractSpecFilter() {

    override fun filterOperation(
        operation: Operation?,
        api: ApiDescription?,
        params: MutableMap<String, MutableList<String>>?,
        cookies: MutableMap<String, String>?,
        headers: MutableMap<String, MutableList<String>>?
    ): Optional<Operation> {
        return super.filterOperation(operation, api, params, cookies, headers)
    }

    override fun filterSchema(
        schema: Schema<*>?,
        params: MutableMap<String, MutableList<String>>?,
        cookies: MutableMap<String, String>?,
        headers: MutableMap<String, MutableList<String>>?
    ): Optional<Schema<Any>> {
        return super.filterSchema(schema, params, cookies, headers)
    }
}