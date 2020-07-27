package calendar.utils

import ombruk.backend.calendar.form.CreateEventForm
import ombruk.backend.calendar.model.Event

fun eventCreateFormFromEvent(event: Event) = CreateEventForm(
    event.startDateTime,
    event.endDateTime,
    event.partner.id,
    event.station.id,
    event.recurrenceRule
)

