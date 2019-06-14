import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.features.*
import io.ktor.request.receiveText
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import workload.PatternRequest
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

var status:String = "waiting"
var workload: Array<PatternRequest>? = null
var listener:String = ""
var endpoint:String = ""
var threads = 1

fun main(args: Array<String>) {
    var port = args.get(0).toInt()

    embeddedServer(Netty, port, module = Application::module).start(wait = true)
}


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {

        get("/api/getStatus") {
            println("Responed " + status)
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText(status, ContentType.Text.Plain)
        }
        put("/api/setWorkload") {
            val content = call.receiveText()
            workload = loadWorkload(content)
            println("Got new workload with " + workload?.size + " items")
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OK", ContentType.Application.Any)
        }
        get("/api/runBenchmark") {
            println("Start benchmark run...")
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OK", ContentType.Application.Json)
        }

        put("/api/setListener") {
            listener = call.receiveText()
            println("New listener is " + listener)
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OK", ContentType.Text.Plain)
        }
        get("/api/clear") {
            workload = null
            listener = ""
            endpoint = ""
            status = "waiting"
            println("Cleared")
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OK", ContentType.Application.Json)
        }

        put("/api/setEndpoint") {
            endpoint = call.receiveText()
            println("New endpoint is " + endpoint)
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OK", ContentType.Text.Plain)
        }

        put("/api/setThreads") {
            threads = call.receiveText().toInt()
            println("New thread number is " + threads)
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OK", ContentType.Text.Plain)
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

fun loadWorkload(workloadAsText : String) : Array<PatternRequest>? {
    val gsonBuilder:GsonBuilder = GsonBuilder()
    val customGson:Gson = gsonBuilder.create();

    var patternConfig = customGson.fromJson(workloadAsText, kotlin.Array<PatternRequest>::class.java)
    return patternConfig
}