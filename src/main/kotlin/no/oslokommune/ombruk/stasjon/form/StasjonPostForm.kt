package no.oslokommune.ombruk.stasjon.form

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.model.serializer.LocalTimeSerializer
import no.oslokommune.ombruk.shared.utils.validation.isUniqueInRepository
import no.oslokommune.ombruk.shared.utils.validation.isValid
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.time.DayOfWeek
import java.time.LocalTime

@Serializable
data class StasjonPostForm(
    @field:Schema(
        required = true,
        nullable = false,
        description = "The name of the Stasjon",
        example = "Haraldrud"
    ) val navn: String,
    @field:Schema(
        required = true,
        nullable = false,
        description = "Describes the days in which the Stasjon is open. " +
                "If a day is not present, the Stasjon is closed. " +
                " The first value of the array denotes the opening time, and the second the closing time",
        example = "{\n" +
                "      \"MONDAY\": [\n" +
                "        \"09:00:00Z\",\n" +
                "        \"20:00:00Z\"\n" +
                "      ],\n" +
                "      \"TUESDAY\": [\n" +
                "        \"08:00:00Z\",\n" +
                "        \"21:00:00Z\"\n" +
                "      ],\n" +
                "      \"WEDNESDAY\": [\n" +
                "        \"09:00:00Z\",\n" +
                "        \"20:00:00Z\"\n" +
                "      ],\n" +
                "      \"THURSDAY\": [\n" +
                "        \"09:00:00Z\",\n" +
                "        \"20:00:00Z\"\n" +
                "      ],\n" +
                "      \"FRIDAY\": [\n" +
                "        \"09:00:00Z\",\n" +
                "        \"20:00:00Z\"\n" +
                "      ]\n" +
                "    }\n"
    ) val aapningstider: Map<DayOfWeek, List<@Serializable(with = LocalTimeSerializer::class) LocalTime>>
) : IForm<StasjonPostForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StasjonPostForm::navn).isNotBlank()
            validate(StasjonPostForm::aapningstider).isValid()
            validate(StasjonPostForm::navn).isUniqueInRepository(StasjonRepository)
        }
    }
}