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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import measurements.Statisticshandler
import workload.PatternRequest
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

var id:Int = -1
var status:String = "waiting"
var workload: Array<PatternRequest>? = null
var endpoint:String = ""
var threads = 1
var executor = Executors.newFixedThreadPool(threads)
val statisticshandler = Statisticshandler()


fun main(args: Array<String>) {
    var port = args.get(0).toInt()

    embeddedServer(Netty, port, module = Application::module).start(wait = true)
}


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {

        get("/api/getStatus") {
            log.info("Responed " + status)
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText(status, ContentType.Text.Plain)
        }
        put("/api/setWorkload") {
            val content = call.receiveText()
            workload = loadWorkload(content)
            statisticshandler.total = workload!!.size
            log.info("Got new workload with " + workload?.size + " items")
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OK", ContentType.Application.Any)
        }
        get("/api/startBenchmark") {
            log.info("Start benchmark run...")
            status = "running"
            val workloadList = workload?.asList()
            statisticshandler.reset()
            executor = Executors.newFixedThreadPool(threads)
            if (workloadList != null) {
                for (patternRequest in workloadList) {
                    val worker = WorkloadRunnable(patternRequest, statisticshandler, endpoint, id)
                    executor.execute(worker)
                }
            }
            GlobalScope.launch {
                executor.shutdown()
                executor.awaitTermination(2, TimeUnit.HOURS)
                log.info("Worker DONE!")
                status = "waiting"
            }
            log.info("Benchmark started.")

            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OK", ContentType.Application.Json)
        }

        put("/api/setListener") {
            statisticshandler.listener = call.receiveText()
            log.info("New listener is " + statisticshandler.listener)
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OK", ContentType.Text.Plain)
        }
        get("/api/clear") {
            workload = null
            statisticshandler.listener = ""
            statisticshandler.reset()
            endpoint = ""
            status = "waiting"
            log.info("Cleared")
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OK", ContentType.Application.Json)
        }

        put("/api/setEndpoint") {
            endpoint = call.receiveText()
            log.info("New endpoint is " + endpoint)
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OK", ContentType.Text.Plain)
        }

        put("/api/setID") {
            id = call.receiveText().toInt()
            log.info("New ID is " + id)
            call.response.header("Access-Control-Allow-Origin", "*")
            call.respondText("OK", ContentType.Text.Plain)
        }

        put("/api/setThreads") {
            threads = call.receiveText().toInt()
            log.info("New thread number is " + threads)
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