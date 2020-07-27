package calendar.utils

import ombruk.backend.calendar.form.CreateEventForm
import ombruk.backend.calendar.model.Event
import java.time.DayOfWeek

fun eventCreateFormFromEvent(event: Event) = CreateEventForm(
    event.startDateTime,
    event.endDateTime,
    event.partner.id,
    event.station.id,
    event.recurrenceRule
)

fun everyWeekDay() = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY
)

fun everyday() = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY
)
