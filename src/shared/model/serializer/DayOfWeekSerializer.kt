package ombruk.backend.shared.model.serializer

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.time.DayOfWeek
import java.time.LocalTime

object DayOfWeekSerializer :
    KSerializer<DayOfWeek> {
    override val descriptor: SerialDescriptor = PrimitiveDescriptor("DayOfWeek", PrimitiveKind.STRING)
    val json = Json(JsonConfiguration.Stable)

    override fun serialize(encoder: Encoder, obj: DayOfWeek) =
        encoder.encodeString(obj.value.toString())


    @ImplicitReflectionSerializer
    override fun deserialize(decoder: Decoder): DayOfWeek =
        DayOfWeek.of(decoder.decodeString().toInt())

}