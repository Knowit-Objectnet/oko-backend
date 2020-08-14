package reporting.form

import arrow.core.Either
import ombruk.backend.reporting.form.ReportGetByIdForm
import ombruk.backend.reporting.form.ReportGetForm
import ombruk.backend.reporting.form.ReportUpdateForm
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReportFormTest {

    @Nested
    inner class Get {

        /**
         * Valid GetById form
         */
        @Test
        fun `valid GetById form`() {
            val form = ReportGetByIdForm(1)
            assert(form.validOrError() is Either.Right)
        }

        /**
         * Invalid GetById form
         */
        @Test
        fun `invalid GetById form`() {
            val form = ReportGetByIdForm(0)
            assert(form.validOrError() is Either.Left)
        }

        /**
         * Valid Get form with all values set but eventId
         */
        @Test
        fun `valid Get form all valid values`() {
            //other values cannot be set when eventId is set. Thus, it is omitted
            val form = ReportGetForm(
                stationId = 1,
                partnerId = 1,
                fromDate = LocalDateTime.parse("2020-07-07T15:15:00Z", DateTimeFormatter.ISO_DATE_TIME),
                toDate = LocalDateTime.parse("2020-07-07T16:15:00Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            assert(form.validOrError() is Either.Right)
        }

        /**
         * Invalid Get with all values set including eventId
         */
        @Test
        fun `invalid Get form all values`() {
            val form = ReportGetForm(
                eventId = 1,
                stationId = 1,
                partnerId = 1,
                fromDate = LocalDateTime.parse("2020-07-07T15:15:00Z", DateTimeFormatter.ISO_DATE_TIME),
                toDate = LocalDateTime.parse("2020-07-07T16:15:00Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            assert(form.validOrError() is Either.Left)
        }

        /**
         * Valid Get form with no values set
         */
        @Test
        fun `valid Get form no values`() {
            val form = ReportGetForm()
            assert(form.validOrError() is Either.Right)
        }

        /**
         * Valid get form eventId set
         */
        @Test
        fun `valid get form eventId set`() {
            val form = ReportGetForm(eventId = 1)
            assert(form.validOrError() is Either.Right)
        }

        /**
         * Invalid get form eventId set
         */
        @Test
        fun `invalid get form eventId set`() {
            val form = ReportGetForm(eventId = 0)
            assert(form.validOrError() is Either.Left)
        }

        /**
         * Valid station ID
         */
        @Test
        fun `valid get form stationId set`() {
            val form = ReportGetForm(stationId = 5)
            assert(form.validOrError() is Either.Right)
        }

        /**
         * Invalid station ID
         */
        @Test
        fun `invalid get form stationId set`() {
            val form = ReportGetForm(stationId = 0)
            assert(form.validOrError() is Either.Left)
        }

        /**
         * Valid partner ID
         */
        @Test
        fun `valid get form partnerId set`() {
            val form = ReportGetForm(partnerId = 1)
            assert(form.validOrError() is Either.Right)
        }

        /**
         * Invalid partner ID
         */
        @Test
        fun `invalid get form partnerId set`() {
            val form = ReportGetForm(partnerId = 0)
            assert(form.validOrError() is Either.Left)
        }

        /**
         * Valid fromDate
         */
        @Test
        fun `valid get form fromDate set`() {
            val form =
                ReportGetForm(fromDate = LocalDateTime.parse("2020-07-07T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME))
            assert(form.validOrError() is Either.Right)
        }

        /**
         * Valid toDate
         */
        @Test
        fun `valid get form toDate set`() {
            val form =
                ReportGetForm(fromDate = LocalDateTime.parse("2020-05-04T19:00:00Z", DateTimeFormatter.ISO_DATE_TIME))
            assert(form.validOrError() is Either.Right)
        }

        /**
         * Valid fromDate and toDate
         */
        @Test
        fun `valid get form fromDate and toDate set`() {
            val form = ReportGetForm(
                fromDate = LocalDateTime.parse("2020-07-07T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                toDate = LocalDateTime.parse("2020-08-08T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            assert(form.validOrError() is Either.Right)
        }

        /**
         * Invalid: fromDate greater than toDate
         */
        @Test
        fun `invalid get form fromDate greater than toDate`() {
            val form = ReportGetForm(
                fromDate = LocalDateTime.parse("2020-08-08T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                toDate = LocalDateTime.parse("2020-07-07T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            assert(form.validOrError() is Either.Left)
        }

        /**
         * Invalid: fromDate equal to toDate
         */
        @Test
        fun `invalid get form fromDate equal to toDate`() {
            val form = ReportGetForm(
                fromDate = LocalDateTime.parse("2020-08-08T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                toDate = LocalDateTime.parse("2020-08-08T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
            )
            assert(form.validOrError() is Either.Left)
        }

        /**
         * Invalid: Event ID and station ID set
         */
        @Test
        fun `invalid get form eventId and stationId set`() {
            val form = ReportGetForm(eventId = 1, stationId = 5)
            assert(form.validOrError() is Either.Left)
        }

        /**
         * Random combination: station ID and toDate set
         */
        @Test
        fun `valid get form stationId and toDate set`() {
            val form = ReportGetForm(stationId = 5, toDate = LocalDateTime.parse("2020-08-08T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME))
            assert(form.validOrError() is Either.Right)
        }
    }

    @Nested
    inner class Update {

        /**
         * Valid Update returns right
         */
        @Test
        fun `valid update returns right`(){
            val form = ReportUpdateForm(1, 50)
            assert(form.validOrError() is Either.Right)
        }

        /**
         * Invalid Update id returns left
         */
        @Test
        fun `invalida update id returns left`(){
            val form = ReportUpdateForm(0, 50)
            assert(form.validOrError() is Either.Left)
        }

        /**
         * Invalid Update weight returns left
         */
        @Test
        fun `invalid update weight returns left`(){
            val form = ReportUpdateForm(1, 0)
            assert(form.validOrError() is Either.Left)
        }

        /**
         * Invalid Update both invalid
         */
        @Test
        fun `invalid update both properties`(){
            val form = ReportUpdateForm(-1, -1)
            assert(form.validOrError() is Either.Left)
        }
    }


}