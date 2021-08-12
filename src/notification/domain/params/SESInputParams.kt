package ombruk.backend.notification.domain.params

data class SESInputParams(
    val subject: String,
    val previewMessage: String,
    val message: String
)
