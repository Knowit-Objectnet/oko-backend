package ombruk.backend.calendar.model.validator

interface ModelValidatorCode {
    val info: String?
}

interface ModelValidator <ModelType> {
    fun validate(model: ModelType): ModelValidatorCode
}
