package measurements

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class Statisticshandler () {

    val log = LoggerFactory.getLogger("Statisticshandler")

    private var count = 0
    var total = 0
    private var lastNotification = 0
    private var notifications:ArrayList<String> = ArrayList()
    private var measurements = ArrayList<PatternMeasurement>()

    @Synchronized
    fun addDone() {
        count++

        val p = count * 100 / total
        if (p >= lastNotification) {
            GlobalScope.launch {
                addNotification("" + p.toString() + "% requests done")
            }
            lastNotification += 10
        }
    }

    @Synchronized
    fun addMeasurement(measurement: PatternMeasurement) {
        measurements.add(measurement)
    }

    fun getMeasurements() : ArrayList<PatternMeasurement> {
        return measurements
    }

    fun reset() {
        count = 0
        lastNotification = 0
        measurements = ArrayList()
        notifications = ArrayList()
    }

    fun addNotification(message: String) {
        notifications.add(message)
    }

    suspend fun getNotitications():ArrayList<String> {
        val copy:ArrayList<String> = notifications.clone() as ArrayList<String>
        notifications = ArrayList()
        log.info("Notifications requested and cleared")
        return copy
    }
}