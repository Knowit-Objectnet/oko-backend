package ombruk.backend.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object Pickups : IntIdTable("pickups") {
    val startTime = datetime("start_time")
    val endTime = datetime("end_time")
    val stationID = integer("station_id").references(Stations.id)
}
