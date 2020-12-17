package no.oslokommune.ombruk.uttak.utils

import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import java.time.temporal.TemporalAdjusters


class PostUttakFormIterator(postForm: UttakPostForm) : Iterator<UttakPostForm> {

    private var currentForm = postForm
    private var currentCount: Int = 1
    private val rRule: GjentakelsesRegel = postForm.gjentakelsesRegel!!
    private var weekStep: Int = rRule.intervall // Intervall of 1 is every week, 2 every other etc...

        init {
            // Make iteration start on the first day of the days list. Next week if start date doesn't correspond with first value in days.
            rRule.dager.let { days ->
                if (days.isNotEmpty() && currentForm.startTidspunkt.dayOfWeek !in days) {
                    val nextStartDate = currentForm.startTidspunkt.with(TemporalAdjusters.next(days.min()))
                    val nextEndDate = currentForm.sluttTidspunkt.with(TemporalAdjusters.next(days.min()))
                    currentForm = currentForm.copy(startTidspunkt = nextStartDate, sluttTidspunkt = nextEndDate)
                }
            }
    }

    override fun hasNext(): Boolean {
        if (rRule.until != null) return (currentForm.startTidspunkt <= rRule.until)
        else if (rRule.antall != null) return (currentCount <= rRule.antall!!)
        //else if (rRule.antall != null) return ((currentCount/rRule.intervall) <= (rRule.antall!! * rRule.intervall))
        return false
    }

    override fun next(): UttakPostForm {

        // Find next day
        val currentDay = currentForm.startTidspunkt.dayOfWeek
        val nextDay = if (rRule.dager.isEmpty()) currentDay else {
            rRule.dager.firstOrNull { it.ordinal > currentDay.ordinal } ?: rRule.dager.min()!!
        }

        // find next start and end date
        var nextStartDate = currentForm.startTidspunkt.with(TemporalAdjusters.next(nextDay))
        var nextEndDate = currentForm.sluttTidspunkt.with(TemporalAdjusters.next(nextDay))

        // Increase count if week is over
        if (rRule.dager.isEmpty()) currentCount++
        else if (nextDay <= currentDay) {
            currentCount++
            if (weekStep > 1) {
                (1 until weekStep).forEach {
                    nextStartDate = nextStartDate.with(TemporalAdjusters.next(nextDay))
                    nextEndDate = nextEndDate.with(TemporalAdjusters.next(nextDay))
                }
            }
        }

        // create next uttak
        val next = currentForm
        currentForm = currentForm.copy(startTidspunkt = nextStartDate, sluttTidspunkt = nextEndDate)

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