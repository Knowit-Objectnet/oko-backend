package ombruk.backend.shared.model.serializer

import kotlinx.serialization.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializer(forClass = LocalDateTime::class)
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveDescriptor("DateAsString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, obj: LocalDateTime) =
        encoder.encodeString(obj.format(DateTimeFormatter.ISO_DATE_TIME) + "Z")


    override fun deserialize(decoder: Decoder): LocalDateTime =
        LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_DATE_TIME)

}