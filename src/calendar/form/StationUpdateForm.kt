package ombruk.backend.calendar.form

import kotlinx.serialization.Serializable

@Serializable
data class StationUpdateForm(val id: Int, val name: String)