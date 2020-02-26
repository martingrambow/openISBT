package webbackend

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.features.*
import io.ktor.request.receiveText
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mapping.Mapper
import mapping.ResourceMapping
import measurement.PatternMeasurement
import run.Worker
import run.Workerhandler
import util.loadOAS
import util.loadPatternConfig
import util.loadWorker
import workload.BackendProgressListener
import workload.PatternRequest
import workload.SchemaFaker
import workload.WorkloadGenerator
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

val workerhandler = Workerhandler()

val oasFiles:MutableMap<Int, String> = HashMap()
val patternConfigs:MutableMap<Int, String> = HashMap()
val resourceMappings:MutableMap<Int, ArrayList<ResourceMapping>> = HashMap()
val workloads:MutableMap<Int, Array<PatternRequest>> = HashMap()
val progressListener:MutableMap<Int, BackendProgressListener> = HashMap()
val workerSets: MutableMap<Int,MutableMap<Int,Worker>> = HashMap()
val results: MutableMap<Int,ArrayList<PatternMeasurement>> = HashMap()

fun main() {
    embeddedServer(Netty, 8080, module = Application::module).start(wait = true)
}


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)

    val notificationLists : MutableMap<Int, ArrayList<ServerNotification>> = HashMap()

    install(Routing) {
        get("/api/ping") {
            val obj = Entry("Hello, World!")
            val gson = Gson()
            val str = gson.toJson(obj)
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText(str, ContentType.Application.Json)
        }
        post("/api/oasFiles") {
            val r = Random()
            var found: Boolean
            var id: Int
            do {
                id = r.nextInt(10000)
                found = oasFiles[id] != null
            } while (found)

            val content = call.receiveText()
            oasFiles[id] = content
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OAS file stored with key $id", ContentType.Application.Any)
        }
        get("/api/oasFiles/{id}") {
            val id = Integer.parseInt(call.parameters["id"])
            val file = oasFiles.getOrDefault(id, "not found")
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText(file, ContentType.Application.Json)
        }

        get("/api/oasFiles/{id}/endpoints") {
            val id = Integer.parseInt(call.parameters["id"])
            val file = oasFiles.getOrDefault(id, "not found")
            val spec:OpenAPISPecifcation? = loadOAS(file)

            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText(GsonBuilder().create().toJson(spec!!.servers), ContentType.Application.Json)
        }

        post("/api/patternConfigs") {
            val r = Random()
            var found: Boolean
            var id: Int
            do {
                id = r.nextInt(10000)
                found = patternConfigs[id] != null
            } while (found)

            val content = call.receiveText()
            patternConfigs[id] = content
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("patternMappingList config stored with key $id", ContentType.Application.Any)
        }
        get("/api/patternConfigs/{id}") {
            val id = Integer.parseInt(call.parameters["id"])
            val file = patternConfigs.getOrDefault(id, "not found")
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText(file, ContentType.Application.Json)
        }

        get("/api/mapping") {
            val oasFileID = Integer.parseInt(call.parameters["oasFile"])
            val patternConfigID = Integer.parseInt(call.parameters["patternConfig"])
            val oasFile = oasFiles.getOrDefault(oasFileID, "")
            val patternConfigFile = patternConfigs.getOrDefault(patternConfigID, "")

            val spec:OpenAPISPecifcation? = loadOAS(oasFile)
            val config:PatternConfiguration? = loadPatternConfig(patternConfigFile)

            if (spec != null && config != null) {
                val mapper = Mapper(spec, config)
                mapper.mapPattern()
                mapper.calculateRequests()

                val r = Random()
                var found: Boolean
                var id: Int
                do {
                    id = r.nextInt(10000)
                    found = resourceMappings[id] != null
                } while (found)

                resourceMappings[id] = mapper.resourceMappings

                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText("Mapping is stored with key $id", ContentType.Application.Any)
            }
        }

        get("/api/mapping/{id}") {
            val id = Integer.parseInt(call.parameters["id"])
            val mapping = resourceMappings.getOrDefault(id, "not found")
            call.response.header("Access-Control-Allow-Origin", "*")

            val gson:Gson = GsonBuilder().create()
            call.respondText(gson.toJson(mapping), ContentType.Application.Json)
        }

        put("/api/mapping/{id}") {
            val mappingID = Integer.parseInt(call.parameters["id"])
            val path = call.parameters["path"]
            var enabled = true
            val patternConfigID = Integer.parseInt(call.parameters["patternConfig"])

            val patternConfigFile = patternConfigs.getOrDefault(patternConfigID, "")
            val config:PatternConfiguration? = loadPatternConfig(patternConfigFile)

            if (call.parameters["enabled"] == "false") {
                enabled = false
            }

            val mappingList = resourceMappings[mappingID]
            if (mappingList != null && config != null) {
                for (mapping in mappingList) {
                    if (mapping.resourcePath == path && mapping.supported) {
                        mapping.enabled = enabled
                    }
                }
                val mapper = Mapper(null, config)
                mapper.resourceMappings = resourceMappings.getValue(mappingID)
                mapper.calculateRequests()
            }

            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("ok", ContentType.Application.Any)
        }

        get ("/api/workload/generate/{id}") {
            val mappingID = Integer.parseInt(call.parameters["id"])
            val mapping: ArrayList<ResourceMapping>? = resourceMappings[mappingID]
            val answer: String
            if (mapping == null) {
                answer = "not found"
            } else {
                val r = Random()
                var found: Boolean
                var id: Int
                do {
                    id = r.nextInt(10000)
                    found = workloads[id] != null
                } while (found)
                answer = id.toString()
                GlobalScope.launch {
                    SchemaFaker.port = 9080
                    val generator = WorkloadGenerator()
                    val newListener = BackendProgressListener()
                    generator.listener =newListener
                    progressListener[id] = newListener
                    generator.generateWorkload(mapping.toTypedArray())
                    SchemaFaker.close()
                    workloads[id] = generator.getWorkload()
                }
            }

            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("Workload is stored with key $answer", ContentType.Application.Any)
        }

        get ("/api/workload/status/{id}") {
            val workloadID = Integer.parseInt(call.parameters["id"])
            val listener = progressListener[workloadID]
            var answer = "not found"
            if (listener != null) {
                answer = listener.currentProgress.toString()
            }

            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText(answer, ContentType.Application.Any)
        }

        get("/api/workload/{workloadID?}") {
            val id = Integer.parseInt(call.parameters["workloadID"])
            val workload = workloads.getOrDefault(id, "not found")
            call.response.header("Access-Control-Allow-Origin", "*")
            val gson:Gson = GsonBuilder().create()
            call.respondText(gson.toJson(workload), ContentType.Application.Json)
        }

        get("/api/run/workerstatus") {
            var answer = "Error"
            if (call.parameters.contains("url") && call.parameters["url"]?.length!! > 0) {
                val w = Worker()
                w.url = call.parameters["url"]!!
                answer = workerhandler.getWorkerStatus(w)
            }
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText(answer, ContentType.Application.Any)
        }

        post("api/run/worker") {
            val r = Random()
            var found: Boolean
            var id: Int
            do {
                id = r.nextInt(10000)
                found = workerSets[id] != null
            } while (found)

            val workerAsText = call.receiveText()
            val workerAsObjects = loadWorker(workerAsText)
            val worker:MutableMap<Int, Worker> = HashMap()
            if (workerAsObjects != null) {
                for (w in workerAsObjects.asList()) {
                    worker[w.id] = w
                }
            }
            var doubleUrls = false
            for (w in worker.values) {
                for (o in worker.values) {
                    if (w.url == o.url && w.id != o.id) {
                        doubleUrls = true
                    }
                }
            }
            if (!doubleUrls) {
                workerSets[id] = worker
                notificationLists[id] = ArrayList()
                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText("Workers are stored with key $id", ContentType.Text.Plain)
            } else {
                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText("ERROR: Doubled URLs", ContentType.Text.Plain)
            }
        }

        get("/api/run/ensureWorkerWaiting/{workersetID?}") {
            val id = Integer.parseInt(call.parameters["workersetID"])
            val allWaiting: Boolean = workerhandler.ensureAllWorkerStatus(workerSets.getOrDefault(id, HashMap()).values.toTypedArray(), "waiting")
            call.response.header("Access-Control-Allow-Origin", "*")
            when {
                allWaiting -> call.respondText("OK", ContentType.Text.Plain)
                !allWaiting -> call.respondText("ERROR (see backend logs for details)", ContentType.Text.Plain)
            }
        }

        get("/api/run/initWorker/{workersetID?}") {
            val id = Integer.parseInt(call.parameters["workersetID"])
            val endpoint: String
            var noErrors = true
            val worker = workerSets.getOrDefault(id, HashMap())
            if (call.parameters.contains("endpoint") && call.parameters["endpoint"]?.length!! > 0 && worker.values.isNotEmpty()) {
                endpoint = call.parameters.get(name = "endpoint")!!
                for (w in worker.values) {
                    if (noErrors && workerhandler.clearWorker(w)) {
                        if (noErrors && workerhandler.setID(w)) {
                            if (noErrors && workerhandler.setEndpoint(w, endpoint)) {
                                if (noErrors && workerhandler.setThreads(w, w.threads)) {
                                    //Everything ok
                                } else {
                                    noErrors = false
                                }
                            } else {
                                noErrors = false
                            }
                        } else {
                            noErrors = false
                        }
                    } else {
                        noErrors = false
                    }
                }
            } else {
                println("No endpoint or workers given")
                noErrors = false
            }
            call.response.header("Access-Control-Allow-Origin", "*")
            when {
                noErrors -> call.respondText("OK", ContentType.Text.Plain)
                !noErrors -> call.respondText("ERROR (see backend logs for details)", ContentType.Text.Plain)
            }
        }

        get("/api/run/distribute/{workersetID?}") {
            val workersetID = Integer.parseInt(call.parameters["workersetID"])

            var workloadID:Int? = -1
            val workload:Array<PatternRequest>?
            if (call.parameters.contains("workload") && call.parameters["workload"]?.length!! > 0) {
                workloadID = call.parameters["workload"]!!.toInt()
            }
            if (workloadID == -1) {
                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText("ERROR, workloadID not given", ContentType.Text.Plain)
            } else {
                workload = workloads[workloadID]
                if (workload == null) {
                    call.response.header("Access-Control-Allow-Origin", "*")
                    call.respondText("ERROR, workload not found", ContentType.Text.Plain)
                } else {
                    if (workerhandler.distributeWorkload(workerSets.getOrDefault(workersetID, HashMap()).values.toTypedArray(), workload)) {
                        call.response.header("Access-Control-Allow-Origin", "*")
                        call.respondText("OK", ContentType.Text.Plain)
                    } else {
                        call.response.header("Access-Control-Allow-Origin", "*")
                        call.respondText("ERROR (see backend logs for details)", ContentType.Text.Plain)
                    }
                }
            }
        }

        get("/api/run/start/{workersetID?}") {
            val workersetID = Integer.parseInt(call.parameters["workersetID"])

            if (workerhandler.startBenchmark(workerSets.getOrDefault(workersetID, HashMap()).values.toTypedArray())) {
                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText("OK", ContentType.Text.Plain)
                //Periodically request notifications from workers
                workerhandler.startNotificationListener(workerSets.getOrDefault(workersetID, HashMap()).values.toTypedArray()
                ) { id: Int, x:String ->
                    notificationLists[workersetID]?.add(ServerNotification(workersetID, id, x))
                }
            } else {
                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText("ERROR (see backend logs for details)", ContentType.Text.Plain)
            }
        }

        get("/api/run/notification/{workersetID?}") {

            val workersetID = Integer.parseInt(call.parameters["workersetID"])
            val list = notificationLists[workersetID]
            notificationLists[workersetID] = ArrayList()
            if (list != null) {
                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText(GsonBuilder().create().toJson(list), ContentType.Text.Plain)
            }
        }

        get ("/api/run/collect/{workersetID?}") {
            val workersetID = Integer.parseInt(call.parameters["workersetID"])

            val measurements = workerhandler.collectResults(workerSets.getOrDefault(workersetID, HashMap()).values.toTypedArray())

            val r = Random()
            var found: Boolean
            var id: Int
            do {
                id = r.nextInt(10000)
                found = results[id] != null
            } while (found)

            results[id] = measurements
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("measurements are stored with key $id", ContentType.Application.Any)
        }

        get("/api/results/{measurementsID?}") {
            val id = Integer.parseInt(call.parameters["measurementsID"])
            val measurements = results.getOrDefault(id, "not found")
            call.response.header("Access-Control-Allow-Origin", "*")
            val gson:Gson = GsonBuilder().create()
            call.respondText(gson.toJson(measurements), ContentType.Application.Json)
        }

        options("/{...}") {
            log.info("OPTIONS CALLED")
            call.response.header("Access-Control-Allow-Origin", "*")
            call.response.header("Access-Control-Allow-Headers", "*")
            call.response.header("Access-Control-Allow-Methods", "*")
            call.respondText("ok", ContentType.Application.Any)
        }
    }
}

data class Entry(val message: String)
data class ServerNotification(val workersetID: Int, val workerID:Int, val message: String)