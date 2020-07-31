package ombruk.backend.calendar.model.validator

import ombruk.backend.calendar.form.event.EventUpdateForm
import java.time.DayOfWeek

object EventUpdateFormValidator :
    ModelValidator<EventUpdateForm> {
    override fun validate(eventUpdate: EventUpdateForm): ModelValidatorCode {
        if(eventUpdate.startDateTime == null|| eventUpdate.endDateTime == null){
            return EventValidatorCode.OK
        }
        if (eventUpdate.startDateTime >= eventUpdate.endDateTime) {
            return EventValidatorCode.StartDateAfterEndDate
        }

        if (eventUpdate.startDateTime.dayOfWeek >= DayOfWeek.SATURDAY || eventUpdate.endDateTime.dayOfWeek >= DayOfWeek.SATURDAY) {
            return EventValidatorCode.DateDuringWeekend
        }

        if (eventUpdate.startDateTime.dayOfYear != eventUpdate.startDateTime.dayOfYear) {
            return EventValidatorCode.MultipleDays
        }

        return EventValidatorCode.OK
    }
}