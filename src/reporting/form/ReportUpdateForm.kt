package ombruk.backend.reporting.form

import kotlinx.serialization.Serializable

@Serializable
data class ReportUpdateForm(val id: Int, val weight: Int)