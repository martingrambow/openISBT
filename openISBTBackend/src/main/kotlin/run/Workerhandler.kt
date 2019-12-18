package run

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.application.log
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
import java.util.concurrent.TimeUnit

class Workerhandler {

    val log = LoggerFactory.getLogger("Workerhandler")

    suspend fun getWorkerStatus(worker: Worker) : String {
        var url:String = buildURL(worker, "/api/getStatus")
        var answer:String = "Error"
        //Make call to worker
        try {
            var client = HttpClient()
            answer = client.get<String>(url)
            client.close()
        } catch (e: ConnectException) {
            answer="Error: Connect exception while connecting to " + url
        }
        return answer
    }

    suspend fun clearWorker(worker: Worker) : Boolean {
        try {
            var client = HttpClient()
            var url = buildURL(worker, "/api/clear")
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

    suspend fun setEndpoint(worker: Worker, endpoint:String) : Boolean {
        try {
            var client = HttpClient()
            var url = buildURL(worker, "/api/setEndpoint")
            val response = client.put<String>(url, {
                body = endpoint
            })
            client.close()
            if (response == "OK") {
                return true
            } else {
                log.error("Error while setEndpoint for worker " + worker.id + ", " + worker.url + ": " + response)
            }
        } catch (e:ConnectException) {
            log.error("Error while setEndpoint for worker " + worker.id + ", " + worker.url + ": " + e.toString())
        }
        return false
    }

    suspend fun setID(worker: Worker) : Boolean {
        try {
            var client = HttpClient()
            var url = buildURL(worker, "/api/setID")
            val response = client.put<String>(url, {
                body = "" + worker.id.toString()
            })
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
            var client = HttpClient()
            var url = buildURL(worker, "/api/setThreads")
            val response = client.put<String>(url, {
                body = threads.toString()
            })
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



    suspend fun ensureAllWorkerStatus(workers : MutableMap<Int, Worker>, status: String) : Boolean {
        if (workers.values.size == 0) {
            log.error("No workers given")
            return false
        }
        var allStatus = true;

        for (w in workers.values) {
            val response = getWorkerStatus(w)
            if (response != status) {
                log.warn("Worker " + w.id + " is not " + status + " but " + response)
                allStatus = false
            }
        }
        if (!allStatus) {
            log.warn("not all workers are " + status)
        }
        return allStatus
    }

    suspend fun distributeWorkload(workers: MutableMap<Int, Worker>, workload:Array<PatternRequest>) : Boolean{
        if (workers.values.size == 0) {
            log.error("No workers given")
            return false
        }
        var packageNumber = workers.values.size
        var packages: ArrayList<ArrayList<PatternRequest>> = ArrayList()

        //Create empty packages
        for (w in workers.values) {
            packages.add(ArrayList())
        }

        //Assign PatternRequests to Packages
        var nextIdx = 0
        for (req in workload) {
            packages.get(nextIdx).add(req)
            nextIdx++
            if (nextIdx >= packages.size) {
                nextIdx = 0
            }
        }

        //Send packages to workers
        for (w in workers.values) {
            var pack = packages.removeAt(0)
            try {
                var client = HttpClient()
                var url = buildURL(w, "/api/setWorkload")
                val response = client.put<String>(url, {
                    body = GsonBuilder().create().toJson(pack)
                })
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

    suspend fun startBenchmark(workers: MutableMap<Int, Worker>) : Boolean{
        if (workers.values.size == 0) {
            log.error("No workers given")
            return false
        }

        //Send packages to workers
        for (w in workers.values) {
            try {
                var client = HttpClient()
                var url = buildURL(w, "/api/startBenchmark")
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

    suspend fun startNotificationListener(workers: MutableMap<Int, Worker>, add: (Int, String) -> Unit) {
        GlobalScope.launch {
            //Request every worker for notifications every 2 seconds
            var end = false
            do {
                delay(2000)
                for (w in workers.values) {
                    try {
                        var client = HttpClient()
                        var url = buildURL(w, "/api/getNotifications")
                        val response = client.get<String>(url)
                        client.close()

                        for (n in loadNotifications(response)) {
                            add(w.id, n)

                            if (n.contains("100%")) {
                                log.debug("Check if all workers are finished...")
                                var allDone = ensureAllWorkerStatus(workers, "waiting")
                                log.debug("All workers finished: " + allDone)
                                if (allDone) {
                                    log.debug("Send server notification..")
                                    add(-1, "All workers finished")
                                    end = true;
                                }
                            }
                        }
                    } catch (e:ConnectException) {
                        log.error("Error while getting notofications from worker " + w.id + ", " + w.url + ": " + e.toString())
                    }
                }
            } while (!end)
        }
    }

    suspend fun collectResults(workers: MutableMap<Int, Worker>) : ArrayList<PatternMeasurement>{
        var results: ArrayList<PatternMeasurement> = ArrayList()

        if (workers.values.size == 0) {
            log.error("No workers given")
            return results
        }

        //Request measurements from workers
        for (w in workers.values) {
            try {
                var client = HttpClient()
                var url = buildURL(w, "/api/getMeasurements")
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
            url = "http://" + url
        }
        return url
    }

    private fun loadMeasurement(measurements: String): Array<PatternMeasurement> {
        val gsonBuilder:GsonBuilder = GsonBuilder()
        val customGson: Gson = gsonBuilder.create()
        var measurement = customGson.fromJson(measurements, Array<PatternMeasurement>::class.java)
        return measurement
    }

    private fun loadNotifications(notifications: String): Array<String> {
        val gsonBuilder:GsonBuilder = GsonBuilder()
        val customGson: Gson = gsonBuilder.create()
        var measurement = customGson.fromJson(notifications, Array<String>::class.java)
        return measurement
    }

}