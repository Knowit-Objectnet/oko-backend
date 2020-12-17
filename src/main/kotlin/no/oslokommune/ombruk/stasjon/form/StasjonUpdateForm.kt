package no.oslokommune.ombruk.stasjon.form

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable
import no.oslokommune.ombruk.stasjon.database.StasjonRepository
import no.oslokommune.ombruk.shared.form.IForm
import no.oslokommune.ombruk.shared.model.serializer.LocalTimeSerializer
import no.oslokommune.ombruk.shared.utils.validation.isUniqueInRepository
import no.oslokommune.ombruk.shared.utils.validation.isValid
import no.oslokommune.ombruk.shared.utils.validation.runCatchingValidation
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotBlank
import org.valiktor.validate
import java.time.DayOfWeek
import java.time.LocalTime

@Serializable
data class StasjonUpdateForm(
    @field:Schema(description = "The ID of the Stasjon to update", required = true) val id: Int,
    @field:Schema(
        description = "The name of the station",
        nullable = false,
        example = "Smestad"
    ) val navn: String? = null,
    @field:Schema(
        description = "The opening hours of the Stasjon.",
        nullable = false,
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
    ) val aapningstider: Map<DayOfWeek, List<@Serializable(with = LocalTimeSerializer::class) LocalTime>>? = null
) : IForm<StasjonUpdateForm> {
    override fun validOrError() = runCatchingValidation {
        validate(this) {
            validate(StasjonUpdateForm::id).isGreaterThan(0)
            validate(StasjonUpdateForm::navn).isNotBlank()
            validate(StasjonUpdateForm::navn).isUniqueInRepository(StasjonRepository)
            validate(StasjonUpdateForm::aapningstider).isValid()

        }
    }
}