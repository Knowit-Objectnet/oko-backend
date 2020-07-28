package ombruk.backend.calendar.form

import arrow.core.*
import kotlinx.serialization.Serializable
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.shared.error.ValidationError
import ombruk.backend.shared.form.IForm
import ombruk.backend.shared.model.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class EventUpdateForm(
    val id: Int,
    @Serializable(with = LocalDateTimeSerializer::class) val startDateTime: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val endDateTime: LocalDateTime? = null
) : IForm<EventUpdateForm> {
    override fun validOrError(): Either<ValidationError, EventUpdateForm> {
        val errors = StringBuilder()
        if (startDateTime == null && endDateTime == null) {
            errors.appendln("Both dates cannot be null!")
        } else if(startDateTime != null && endDateTime != null){
            if(startDateTime >= endDateTime){
                errors.appendln("start-date-time cannot be greater than end-date-time!")
            } else if(endDateTime <= startDateTime){
                errors.appendln("end-date-time cannot be lesser than start-date-time")
            }
        }

        when (val event = EventRepository.getEventByID(id)) {
            is Either.Left -> errors.appendln("ID $id does not exist")
            is Either.Right -> {
                if(endDateTime == null && startDateTime != null && startDateTime >= event.b.endDateTime) {
                    errors.appendln("start-date-time cannot be greater than end-date-time!")
                }
                if(startDateTime == null && endDateTime != null && endDateTime <= event.b.startDateTime){
                    errors.appendln("end-date-time cannot be lesser than start-date-time")
                }
            }
        }
        return if(!errors.isBlank()){
            ValidationError.Unprocessable(errors.toString()).left()
        } else{
            this.right()
        }
    }
}