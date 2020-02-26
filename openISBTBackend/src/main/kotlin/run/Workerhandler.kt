package run

import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.put
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import measurement.PatternMeasurement
import org.slf4j.LoggerFactory
import workload.PatternRequest
import java.net.ConnectException

class Workerhandler {

    val log = LoggerFactory.getLogger("Workerhandler")!!

    suspend fun getWorkerStatus(worker: Worker) : String {
        val url:String = buildURL(worker, "/api/getStatus")
        var answer: String
        //Make call to worker
        try {
            val client = HttpClient()
            answer = client.get(url)
            client.close()
        } catch (e: ConnectException) {
            answer= "Error: Connect exception while connecting to $url"
        }
        return answer
    }

    suspend fun clearWorker(worker: Worker) : Boolean {
        try {
            val client = HttpClient()
            val url = buildURL(worker, "/api/clear")
            val response = client.get<String>(url)
            client.close()
            if (response == "OK") {
                return true
            } else {
                log.error("Error while clearing worker " + worker.id + ", " + worker.url + ": " + response)
            }
        } catch (e:ConnectException) {
            log.error("Error while clearing worker " + worker.id + ", " + worker.url + ": " + e.toString())
        }
        return false
    }

    suspend fun setID(worker: Worker) : Boolean {
        try {
            val client = HttpClient()
            val url = buildURL(worker, "/api/setID")
            val response = client.put<String>(url) {
                body = "" + worker.id.toString()
            }
            client.close()
            if (response == "OK") {
                return true
            } else {
                log.error("Error while setID for worker " + worker.id + ", " + worker.url + ": " + response)
            }
        } catch (e:ConnectException) {
            log.error("Error while setID for worker " + worker.id + ", " + worker.url + ": " + e.toString())
        }
        return false
    }

    suspend fun setThreads(worker: Worker, threads:Int) : Boolean {
        try {
            val client = HttpClient()
            val url = buildURL(worker, "/api/setThreads")
            val response = client.put<String>(url) {
                body = threads.toString()
            }
            client.close()
            if (response == "OK") {
                return true
            } else {
                log.error("Error while setThreads for worker " + worker.id + ", " + worker.url + ": " + response)
            }
        } catch (e:ConnectException) {
            log.error("Error while setThreads for worker " + worker.id + ", " + worker.url + ": " + e.toString())
        }
        return false
    }



    suspend fun ensureAllWorkerStatus(workers : Array<Worker>, status: String) : Boolean {
        if (workers.isEmpty()) {
            log.error("No workers given")
            return false
        }
        var allStatus = true

        for (w in workers) {
            val response = getWorkerStatus(w)
            if (response != status) {
                log.warn("Worker " + w.id + " is not " + status + " but " + response)
                allStatus = false
            }
        }
        if (!allStatus) {
            log.warn("not all workers are $status")
        }
        return allStatus
    }

    suspend fun distributeWorkload(workers: Array<Worker>, workload:Array<PatternRequest>) : Boolean{
        if (workers.isEmpty()) {
            log.error("No workers given")
            return false
        }
        val packages: ArrayList<ArrayList<PatternRequest>> = ArrayList()

        //Create empty packages
        for (w in workers) {
            packages.add(ArrayList())
        }

        //Assign PatternRequests to Packages
        var nextIdx = 0
        for (req in workload) {
            packages[nextIdx].add(req)
            nextIdx++
            if (nextIdx >= packages.size) {
                nextIdx = 0
            }
        }

        //Send packages to workers
        for (w in workers) {
            val pack = packages.removeAt(0)
            try {
                val client = HttpClient()
                val url = buildURL(w, "/api/setWorkload")
                val response = client.put<String>(url) {
                    body = GsonBuilder().create().toJson(pack)
                }
                client.close()
                if (response == "OK") {
                    log.info("Send workload (" + pack.size + "items) to worker " + w.id + ", " + w.url)
                } else {
                    log.error("Error while setWorkload for worker " + w.id + ", " + w.url + ": " + response)
                    return false
                }
            } catch (e:ConnectException) {
                log.error("Error while setWorkload for worker " + w.id + ", " + w.url + ": " + e.toString())
                return false
            }
        }

        return true
    }

    suspend fun startBenchmark(workers: Array<Worker>) : Boolean{
        if (workers.isEmpty()) {
            log.error("No workers given")
            return false
        }

        for (w in workers) {
            try {
                val client = HttpClient()
                val url = buildURL(w, "/api/startBenchmark")
                val response = client.get<String>(url)
                client.close()
                if (response == "OK") {
                    log.info("Benchmarked started at worker " + w.id + ", " + w.url)
                } else {
                    log.error("Error while starting benchmark run for worker " + w.id + ", " + w.url + ": " + response)
                    return false
                }
            } catch (e:ConnectException) {
                log.error("Error while starting benchmark run for worker " + w.id + ", " + w.url + ": " + e.toString())
                return false
            }
        }
        return true
    }

    fun startNotificationListener(workers: Array<Worker>, add: (Int, String) -> Unit) {
        GlobalScope.launch {
            //Request every worker for notifications every 2 seconds
            var end = false
            do {
                delay(2000)
                for (w in workers) {
                    try {
                        val client = HttpClient()
                        val url = buildURL(w, "/api/getNotifications")
                        val response = client.get<String>(url)
                        client.close()

                        for (n in loadNotifications(response)) {
                            add(w.id, n)

                            if (n.contains("100%")) {
                                log.debug("Check if all workers are finished...")
                                val allDone = ensureAllWorkerStatus(workers, "waiting")
                                log.debug("All workers finished: $allDone")
                                if (allDone) {
                                    log.debug("Send server notification..")
                                    add(-1, "All workers finished")
                                    end = true
                                }
                            }
                        }
                    } catch (e:ConnectException) {
                        log.error("Error while getting notofications from worker " + w.id + ", " + w.url + ": " + e.toString())
                    }
                }
            } while (!end)
            add(-1, "done")
        }
    }

    suspend fun collectResults(workers: Array<Worker>) : ArrayList<PatternMeasurement>{
        val results: ArrayList<PatternMeasurement> = ArrayList()

        if (workers.isEmpty()) {
            log.error("No workers given")
            return results
        }

        //Request measurements from workers
        for (w in workers) {
            try {
                val client = HttpClient()
                val url = buildURL(w, "/api/getMeasurements")
                val response = client.get<String>(url)

                for (m in loadMeasurement(response)) {
                    results.add(m)
                }

                client.close()

            } catch (e:ConnectException) {
                log.error("Error while starting benchmark run for worker " + w.id + ", " + w.url + ": " + e.toString())
            }
        }

        return results
    }

    private fun buildURL(w: Worker, path:String) : String{
        var url:String = w.url + path
        if (!url.startsWith("http://")) {
            url = "http://$url"
        }
        return url
    }

    private fun loadMeasurement(measurements: String): Array<PatternMeasurement> {
        val customGson = GsonBuilder().create()
        return customGson.fromJson(measurements, Array<PatternMeasurement>::class.java)
    }

    private fun loadNotifications(notifications: String): Array<String> {
        val customGson = GsonBuilder().create()
        return customGson.fromJson(notifications, Array<String>::class.java)
    }

}