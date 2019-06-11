import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializer
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
import workload.PatternRequest
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

val oasFiles:MutableMap<Int, String> = HashMap()
val patternConfigs:MutableMap<Int, String> = HashMap()
val resourceMappings:MutableMap<Int, ArrayList<ResourceMapping>> = HashMap()
val workloads:MutableMap<Int, Array<PatternRequest>> = HashMap()


fun main(args: Array<String>) {

    embeddedServer(Netty, 8080, module = Application::module).start(wait = true)
}


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {
        get("/api/ping/{count?}") {
            var count: Int = Integer.valueOf(call.parameters["count"]?: "1")
            if (count < 1) {
                count = 1
            }
            var obj = Array<Entry>(count, {i -> Entry("$i: Hello, World!")})
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

fun readOASfile(fileName: String): String
        = File("openISBTBackend/src/main/resources/oasFiles/" + fileName).readText(Charsets.UTF_8)

fun loadOAS(oasFile:String):OpenAPISPecifcation? {

    val gsonBuilder:GsonBuilder = GsonBuilder()
    val pathsObjectDeserializer:JsonDeserializer<PathsObject> = PathsObjectDeserializer()
    gsonBuilder.registerTypeAdapter(PathsObject::class.java, pathsObjectDeserializer)
    val responsesObjectDeserializer:JsonDeserializer<ResponsesObject> = ResponsesObjectDeserializer()
    gsonBuilder.registerTypeAdapter(ResponsesObject::class.java, responsesObjectDeserializer)

    val customGson:Gson = gsonBuilder.create();

    var openAPISpec = customGson.fromJson(oasFile, OpenAPISPecifcation::class.java)
    return openAPISpec
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