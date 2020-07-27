package ombruk.backend.model.validator

import ombruk.backend.model.Event
import java.time.DayOfWeek

object EventValidator : ModelValidator<Event> {
    override fun validate(event: Event): ModelValidatorCode {
        if (event.startDateTime >= event.endDateTime) {
            return EventValidatorCode.StartDateAfterEndDate
        }

        if (event.startDateTime.dayOfWeek >= DayOfWeek.SATURDAY || event.endDateTime.dayOfWeek >= DayOfWeek.SATURDAY) {
            return EventValidatorCode.DateDuringWeekend
        }

        if (event.startDateTime.dayOfYear != event.startDateTime.dayOfYear) {
            return EventValidatorCode.MultipleDays
        }

        event.recurrenceRule?.let {
            val rRuleValidatorCode = RecurrenceRuleValidator.validate(it)
            if (rRuleValidatorCode != RecurrenceRuleValidatorCode.OK) {
                return rRuleValidatorCode
            }

        }

        return EventValidatorCode.OK
    }
}

enum class EventValidatorCode : ModelValidatorCode {
    OK {
        override val info: String? = null
    },
    StartDateAfterEndDate {
        override val info = "Your start date can't be after your end date"
    },
    DateDuringWeekend {
        override val info = "Your event can't take place during the weekend"
    },
    MultipleDays {
        override val info = "Your event can't last multiple days"
    }
}