package no.oslokommune.ombruk.shared.model.serializer

import kotlinx.serialization.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializer(forClass = LocalDateTime::class)
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveDescriptor("DateAsString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, obj: LocalDateTime) =
        //encoder.encodeString(obj.format(DateTimeFormatter.ISO_DATE_TIME))
        encoder.encodeString(obj.format(DateTimeFormatter.ISO_DATE_TIME) + "Z")
    // TODO: Necessary with "Z"?


    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_DATE_TIME)
    }
}