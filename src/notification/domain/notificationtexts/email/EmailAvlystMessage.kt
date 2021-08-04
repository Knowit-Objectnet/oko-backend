package notificationtexts.email

import ombruk.backend.aarsak.domain.entity.Aarsak
import ombruk.backend.henting.domain.entity.EkstraHenting
import ombruk.backend.henting.domain.entity.PlanlagtHenting
import ombruk.backend.notification.domain.notificationtexts.Signature
import ombruk.backend.notification.domain.params.SESInputParams
import ombruk.backend.notification.domain.params.SNSInputParams
import ombruk.backend.shared.utils.formatDateRange
import java.time.format.DateTimeFormatter

class EmailAvlystMessage {
    companion object {
        fun getMessage(henting: PlanlagtHenting, aarsak: Aarsak): String {
            return "Din henting hos ${henting.stasjonNavn} stasjon " +
                    "${formatDateRange(henting.startTidspunkt, henting.sluttTidspunkt)} " +
                    "er avlyst grunnet ${aarsak.beskrivelse}." +
                    "\n\nBeklager ulempen, håper å se deg igjen snart!" + Signature.signature
        }

        fun getPreviewMessage(henting: PlanlagtHenting, aarsak: Aarsak): String {
            return "Din henting hos ${henting.stasjonNavn} stasjon " +
                    "${henting.startTidspunkt.format(DateTimeFormatter.ofPattern("dd.MM.yy"))} " +
                    "er avlyst!"
        }

        fun getSubject(): String = "Avlyst henting!"

        fun getInputParams(henting: PlanlagtHenting, aarsak: Aarsak): SESInputParams {
            return SESInputParams(getSubject(), getPreviewMessage(henting, aarsak), getMessage(henting, aarsak))
        }
    }
}

