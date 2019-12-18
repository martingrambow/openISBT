import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonObject
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathsObject
import de.tuberlin.mcc.patternconfiguration.PatternConfiguration
import io.ktor.application.call
import io.ktor.http.ContentType
import openapispecification.ResponsesObject
import openapispecification.deserializer.PathsObjectDeserializer
import openapispecification.deserializer.ResponsesObjectDeserializer
import java.io.File
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.features.*
import io.ktor.request.receiveText
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mapping.Mapper
import mapping.ResourceMapping
import measurement.PatternMeasurement
import run.Worker
import run.Workerhandler
import workload.PatternRequest
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

val workerhandler = Workerhandler()

val oasFiles:MutableMap<Int, String> = HashMap()
val patternConfigs:MutableMap<Int, String> = HashMap()
val resourceMappings:MutableMap<Int, ArrayList<ResourceMapping>> = HashMap()
val workloads:MutableMap<Int, Array<PatternRequest>> = HashMap()
val workerSets: MutableMap<Int,MutableMap<Int,Worker>> = HashMap()
val results: MutableMap<Int,ArrayList<PatternMeasurement>> = HashMap()

fun main(args: Array<String>) {
    if (args.size > 0) {
        Workerhandler.host = args[0]
    }
    embeddedServer(Netty, 8080, module = Application::module).start(wait = true)
}


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)

    val notificationLists : MutableMap<Int, ArrayList<ServerNotification>> = HashMap()

    install(Routing) {
        get("/api/ping") {
            var count: Int = Integer.valueOf(call.parameters["count"]?: "1")
            var obj = Entry("Hello, World!")
            val gson = Gson()
            var str = gson.toJson(obj)
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText(str, ContentType.Application.Json)
        }
        post("/api/oasFiles") {
            val r = Random()
            var found = false;
            var id: Int;
            do {
                id = r.nextInt(10000);
                if (oasFiles.get(id) != null) {
                    found = true;
                } else {
                    found = false;
                }
            } while (found)

            val content = call.receiveText()
            oasFiles.put(id, content)
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OAS file stored with key " + id, ContentType.Application.Any)
        }
        get("/api/oasFiles/{id}") {
            val id = Integer.parseInt(call.parameters.get("id"))
            val file = oasFiles.getOrDefault(id, "not found")
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText(file, ContentType.Application.Json)
        }

        get("/api/oasFiles/{id}/endpoints") {
            val id = Integer.parseInt(call.parameters.get("id"))
            val file = oasFiles.getOrDefault(id, "not found")
            val spec:OpenAPISPecifcation? = loadOAS(file)

            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText(GsonBuilder().create().toJson(spec!!.servers), ContentType.Application.Json)
        }

        post("/api/patternConfigs") {
            val r = Random()
            var found = false;
            var id: Int;
            do {
                id = r.nextInt(10000);
                if (patternConfigs.get(id) != null) {
                    found = true;
                } else {
                    found = false;
                }
            } while (found)

            val content = call.receiveText()
            patternConfigs.put(id, content)
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("patternMappingList config stored with key " + id, ContentType.Application.Any)
        }
        get("/api/patternConfigs/{id}") {
            val id = Integer.parseInt(call.parameters.get("id"))
            val file = patternConfigs.getOrDefault(id, "not found")
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText(file, ContentType.Application.Json)
        }

        get("/api/mapping") {
            val oasFileID = Integer.parseInt(call.parameters.get("oasFile"))
            val patternConfigID = Integer.parseInt(call.parameters.get("patternConfig"))
            val oasFile = oasFiles.getOrDefault(oasFileID, "")
            val patternConfigFile = patternConfigs.getOrDefault(patternConfigID, "")

            val spec:OpenAPISPecifcation? = loadOAS(oasFile)
            val config:PatternConfiguration? = loadPatternConfig(patternConfigFile)

            if (spec != null && config != null) {
                val mapper = Mapper()
                val mapping = mapper.mapPattern(spec, config)

                val r = Random()
                var found = false;
                var id: Int;
                do {
                    id = r.nextInt(10000);
                    if (resourceMappings.get(id) != null) {
                        found = true;
                    } else {
                        found = false;
                    }
                } while (found)

                resourceMappings.put(id, mapping)

                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText("Mapping is stored with key " + id, ContentType.Application.Any)
            }
        }

        get("/api/mapping/{id}") {
            val id = Integer.parseInt(call.parameters.get("id"))
            val mapping = resourceMappings.getOrDefault(id, "not found")
            call.response.header("Access-Control-Allow-Origin", "*")

            val gson:Gson = GsonBuilder().create()
            call.respondText(gson.toJson(mapping), ContentType.Application.Json)
        }

        put("/api/mapping/{id}") {
            val mappingID = Integer.parseInt(call.parameters.get("id"))
            val path = call.parameters.get("path")
            var enabled = true;
            val patternConfigID = Integer.parseInt(call.parameters.get("patternConfig"))

            val patternConfigFile = patternConfigs.getOrDefault(patternConfigID, "")
            val config:PatternConfiguration? = loadPatternConfig(patternConfigFile)

            if (call.parameters.get("enabled") == "false") {
                enabled = false
            }

            var mappingList = resourceMappings.get(mappingID)
            if (mappingList != null && config != null) {
                for (mapping in mappingList) {
                    if (mapping.resourcePath == path && mapping.supported) {
                        mapping.enabled = enabled
                    }
                }
                resourceMappings.put(mappingID, Mapper().calculateRequests(mappingList, config))
            }

            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("ok", ContentType.Application.Any)
        }

        post("/api/workload") {
            val r = Random()
            var found = false;
            var id: Int;
            do {
                id = r.nextInt(10000);
                if (workloads.get(id) != null) {
                    found = true;
                } else {
                    found = false;
                }
            } while (found)

            val workloadAsText = call.receiveText()
            val workload = loadWorkload(workloadAsText)
            if (workload != null) {
                workloads.put(id, workload)
                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText("Workload is stored with key " + id, ContentType.Application.Any)
            } else {
                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText("Unable to store workload", ContentType.Application.Any)
            }

        }

        get("/api/workload/{workloadID?}") {
            val id = Integer.parseInt(call.parameters.get("workloadID"))

            val workload = workloads.getOrDefault(id, "not found")
            call.response.header("Access-Control-Allow-Origin", "*")
            val gson:Gson = GsonBuilder().create()
            call.respondText(gson.toJson(workload), ContentType.Application.Json)
        }

        get("/api/run/workerstatus") {
            var url:String? = ""
            var answer:String = "Error"
            if (call.parameters.contains("url") && call.parameters.get("url")?.length!! > 0) {
                var w:Worker = Worker()
                w.url = call.parameters.get("url")!!
                answer = workerhandler.getWorkerStatus(w)
            }
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText(answer, ContentType.Application.Any)
        }

        post("api/run/worker") {
            val r = Random()
            var found = false;
            var id: Int;
            do {
                id = r.nextInt(10000);
                if (workerSets.get(id) != null) {
                    found = true;
                } else {
                    found = false;
                }
            } while (found)

            val workerAsText = call.receiveText()
            val workerAsObjects = loadWorker(workerAsText)
            var worker:MutableMap<Int, Worker> = HashMap()
            if (workerAsObjects != null) {
                for (w in workerAsObjects.asList()) {
                    worker.put(w.id, w)
                }
            }
            var doubleUrls = false;
            for (w in worker.values) {
                for (o in worker.values) {
                    if (w.url == o.url && w.id != o.id) {
                        doubleUrls = true
                    }
                }
            }
            if (!doubleUrls) {
                workerSets.put(id, worker)
                notificationLists.put(id, ArrayList<ServerNotification>())

                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText("Workers are stored with key " + id, ContentType.Text.Plain)
            } else {
                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText("ERROR: Doubled URLs", ContentType.Text.Plain)
            }
        }

        get("/api/run/ensureWorkerWaiting/{workersetID?}") {
            val id = Integer.parseInt(call.parameters.get("workersetID"))
            var allWaiting = workerhandler.ensureAllWorkerStatus(workerSets.getOrDefault(id, HashMap()), "waiting")
            call.response.header("Access-Control-Allow-Origin", "*")
            when {
                allWaiting -> call.respondText("OK", ContentType.Text.Plain)
                !allWaiting -> call.respondText("ERROR (see backend logs for details)", ContentType.Text.Plain)
            }
        }

        get("/api/run/initWorker/{workersetID?}") {
            val id = Integer.parseInt(call.parameters.get("workersetID"))
            var endpoint:String? = ""
            var noErrors = true
            var worker = workerSets.getOrDefault(id, HashMap())
            if (call.parameters.contains("endpoint") && call.parameters.get("endpoint")?.length!! > 0 && worker.values.size > 0) {
                endpoint = call.parameters.get("endpoint")
                for (w in worker.values) {
                    if (noErrors && workerhandler.clearWorker(w)) {
                        if(noErrors && workerhandler.setListener(w, id)) {
                            if (noErrors && workerhandler.setID(w)) {
                                if (noErrors && workerhandler.setEndpoint(w, endpoint!!)) {
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
            val workersetID = Integer.parseInt(call.parameters.get("workersetID"))

            var workloadID:Int? = -1
            var workload:Array<PatternRequest>?
            if (call.parameters.contains("workload") && call.parameters.get("workload")?.length!! > 0) {
                workloadID = call.parameters.get("workload")!!.toInt()
            }
            if (workloadID == -1) {
                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText("ERROR, workloadID not given", ContentType.Text.Plain)
            } else {
                workload = workloads.get(workloadID)
                if (workload == null) {
                    call.response.header("Access-Control-Allow-Origin", "*")
                    call.respondText("ERROR, workload not found", ContentType.Text.Plain)
                } else {
                    if (workerhandler.distributeWorkload(workerSets.getOrDefault(workersetID, HashMap()), workload)) {
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
            val workersetID = Integer.parseInt(call.parameters.get("workersetID"))

            if (workerhandler.startBenchmark(workerSets.getOrDefault(workersetID, HashMap()))) {
                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText("OK", ContentType.Text.Plain)
            } else {
                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText("ERROR (see backend logs for details)", ContentType.Text.Plain)
            }
        }

        post("api/run/notification/{workersetID?}/{workerID?}") {

            val workersetID = Integer.parseInt(call.parameters.get("workersetID"))
            val workerID = Integer.parseInt(call.parameters.get("workerID"))
            val message = call.receiveText()
            println("Got notification from set " + workersetID + ", worker " + workerID + ": " + message)

            val list = notificationLists.get(workersetID)
            if (list != null) {
                list.add(ServerNotification(workersetID, workerID, message))
            }

            if (message.contains("100%")) {
                log.debug("Check if all workers are finished...")
                var allDone = workerhandler.ensureAllWorkerStatus(workerSets.getOrDefault(workersetID, HashMap()), "waiting")
                log.debug("All workers finished: " + allDone)
                if (allDone && list != null) {
                    log.debug("Send server notification..")
                    list.add(ServerNotification(workersetID, -1, "All workers finished"))
                }
            }

            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OK", ContentType.Text.Plain)
        }

        get("/api/run/notification/{workersetID?}") {

            val workersetID = Integer.parseInt(call.parameters.get("workersetID"))
            val list = notificationLists.get(workersetID)
            notificationLists.put(workersetID, ArrayList())
            if (list != null) {
                call.response.header("Access-Control-Allow-Origin", "*")
                call.respondText(GsonBuilder().create().toJson(list), ContentType.Text.Plain)
            }
        }

        get ("/api/run/collect/{workersetID?}") {
            val workersetID = Integer.parseInt(call.parameters.get("workersetID"))

            var measurements = workerhandler.collectResults(workerSets.getOrDefault(workersetID, HashMap()))

            val r = Random()
            var found = false;
            var id: Int;
            do {
                id = r.nextInt(10000);
                if (results.get(id) != null) {
                    found = true;
                } else {
                    found = false;
                }
            } while (found)

            results.put(id, measurements)
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("measurements are stored with key " + id, ContentType.Application.Any)
        }

        get("/api/results/{measurementsID?}") {
            val id = Integer.parseInt(call.parameters.get("measurementsID"))

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

fun readOASfile(fileName: String): String
        = File("openISBTBackend/src/main/resources/oasFiles/" + fileName).readText(Charsets.UTF_8)

fun loadOAS(oasFile:String):OpenAPISPecifcation? {

    if (oasFile.length > 15) {

        val gsonBuilder: GsonBuilder = GsonBuilder()
        val pathsObjectDeserializer: JsonDeserializer<PathsObject> = PathsObjectDeserializer()
        gsonBuilder.registerTypeAdapter(PathsObject::class.java, pathsObjectDeserializer)
        val responsesObjectDeserializer: JsonDeserializer<ResponsesObject> = ResponsesObjectDeserializer()
        gsonBuilder.registerTypeAdapter(ResponsesObject::class.java, responsesObjectDeserializer)

        val customGson: Gson = gsonBuilder.create();

        var openAPISpec = customGson.fromJson(oasFile, OpenAPISPecifcation::class.java)
        return openAPISpec
    }
    return null
}

fun loadPatternConfig(patternConfigFile: String): PatternConfiguration? {
    val gsonBuilder:GsonBuilder = GsonBuilder()
    val customGson:Gson = gsonBuilder.create();

    var patternConfig = customGson.fromJson(patternConfigFile, PatternConfiguration::class.java)
    return patternConfig
}

fun loadWorkload(workloadAsText : String) : Array<PatternRequest>? {
    val gsonBuilder:GsonBuilder = GsonBuilder()
    val customGson:Gson = gsonBuilder.create();

    var patternConfig = customGson.fromJson(workloadAsText, Array<PatternRequest>::class.java)
    return patternConfig
}

fun loadWorker(workerAsText : String) : Array<Worker>? {
    val gsonBuilder:GsonBuilder = GsonBuilder()
    val customGson:Gson = gsonBuilder.create();

    var worker = customGson.fromJson(workerAsText, Array<Worker>::class.java)
    return worker
}
