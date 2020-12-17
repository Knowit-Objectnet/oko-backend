package no.oslokommune.ombruk.shared.swagger.annotations

import kotlin.reflect.KClass

annotation class ParameterFile(
    val parameters: KClass<*>
)