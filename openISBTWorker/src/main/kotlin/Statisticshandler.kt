import io.ktor.client.HttpClient
import io.ktor.client.request.post
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.net.ConnectException

class Statisticshandler () {

    val log = LoggerFactory.getLogger("Statisticshandler")

    var listener: String = ""
    private var count = 0
    var total = 0
    private var lastNotification = 0

    @Synchronized
    fun addDone() {
        count++

        val p = count * 100 / total
        if (p >= lastNotification) {
            GlobalScope.launch {
                notifyListener("" + p.toString() + "% requests done")
            }
            lastNotification += 10
        }


    }

    fun reset() {
        count = 0
        lastNotification = 0
    }

    suspend fun notifyListener(message: String) {
        log.info("notifying listener: " + message)
        try {
            var client = HttpClient()
            var url = listener
            val response = client.post<String>(url, {
                body = message
            })
            client.close()
            if (response != "OK") {
                log.error("Unable to notify listener, url = " + url)
            }
        } catch (e: ConnectException) {
            log.error("Unable to notify listener " + listener)
        }
    }
}