package notificationtexts.email

import ombruk.backend.aarsak.domain.entity.Aarsak
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.notification.domain.notificationtexts.Signature
import ombruk.backend.notification.domain.params.SESInputParams
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.shared.utils.formatDateRange
import java.time.format.DateTimeFormatter

class EmailAvlystMessageToStasjon {
    companion object {
        fun getMessage(henting: PlanlagtHenting, aarsak: Aarsak): String {
            return "${getAvlystAvNavn(henting)} har avlyst henting " +
                    "${formatDateRange(henting.startTidspunkt, henting.sluttTidspunkt)} " +
                    "grunnet ${aarsak.beskrivelse}." + Signature.signature
        }

        fun getPreviewMessage(henting: PlanlagtHenting, aarsak: Aarsak): String {
            return "${getAvlystAvNavn(henting)} har avlyst henting " +
                    "${formatDateRange(henting.startTidspunkt, henting.sluttTidspunkt)} " +
                    "grunnet ${aarsak.beskrivelse}."
        }

        fun getSubject(henting: PlanlagtHenting): String = "${getAvlystAvNavn(henting)} har avlyst henting " +
                "${formatDateRange(henting.startTidspunkt, henting.sluttTidspunkt)}"

        fun getInputParams(henting: PlanlagtHenting, aarsak: Aarsak): SESInputParams {
            return SESInputParams(getSubject(henting), getPreviewMessage(henting, aarsak), getMessage(henting, aarsak))
        }

        private fun getAvlystAvNavn(henting: PlanlagtHenting): String {
            if (henting.avlystAv == null) return "Unknown"
            else {
                if (henting.avlystAv == henting.stasjonId) return (henting.stasjonNavn + " stasjon")
                else if (henting.avlystAv == henting.aktorId) return henting.aktorNavn
                else return "Administrator"
            }
        }
    }
}

