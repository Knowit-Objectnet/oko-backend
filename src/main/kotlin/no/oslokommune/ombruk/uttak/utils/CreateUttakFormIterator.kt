package no.oslokommune.ombruk.uttak.utils

import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import java.time.temporal.TemporalAdjusters


class CreateUttakFormIterator(postForm: UttakPostForm) : Iterator<UttakPostForm> {

    private var currentForm = postForm
    private var currentCount: Int = 1
    private val rRule: GjentakelsesRegel = postForm.gjentakelsesRegel!!

    init {
        // Make iteration start on the first day of the days list. Next week if start date doesn't correspond with first value in days.
        rRule.days?.let { days ->
            if (currentForm.startDateTime.dayOfWeek !in days) {
                val nextStartDate = currentForm.startDateTime.with(TemporalAdjusters.next(days.min()))
                val nextEndDate = currentForm.endDateTime.with(TemporalAdjusters.next(days.min()))
                currentForm = currentForm.copy(startDateTime = nextStartDate, endDateTime = nextEndDate)
            }
        }
    }

    override fun hasNext(): Boolean {
        if (rRule.until != null) return (currentForm.startDateTime <= rRule.until)
        else if (rRule.count != null) return (currentCount <= rRule.count)
        return false
    }

    override fun next(): UttakPostForm {

        // Find next day
        val currentDay = currentForm.startDateTime.dayOfWeek
        val nextDay = if (rRule.days == null) currentDay else {
            rRule.days.firstOrNull { it.ordinal > currentDay.ordinal } ?: rRule.days.min()!!
        }

        // find next start and end date
        val nextStartDate = currentForm.startDateTime.with(TemporalAdjusters.next(nextDay))
        val nextEndDate = currentForm.endDateTime.with(TemporalAdjusters.next(nextDay))

        //Increase count if week is over
        if (rRule.days == null) currentCount++
        else if (nextDay <= currentDay) currentCount++

        // create next uttak
        val next = currentForm
        currentForm = currentForm.copy(startDateTime = nextStartDate, endDateTime = nextEndDate)

        return next
    }
}

class NonRecurringCreateUttakFormIterator(val postForm: UttakPostForm) : Iterator<UttakPostForm> {
    var hasNext = true
    override fun hasNext(): Boolean {
        val result = hasNext
        hasNext = false
        return result
    }

    override fun next(): UttakPostForm {
        return postForm
    }
}