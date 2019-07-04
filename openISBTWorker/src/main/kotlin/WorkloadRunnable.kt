import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.*
import io.ktor.http.content.TextContent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import workload.PatternRequest
import java.lang.Exception
import java.net.URLEncoder

class WorkloadRunnable(var patternRequest: PatternRequest, val statisticshandler: Statisticshandler, val endpoint:String) : Runnable {

    override fun run() {
        println("Some request for Pattern " + patternRequest.abstractPattern.name + ":" + patternRequest.id + ", " + patternRequest.apiRequests.size + " API requests")

        for (i in 0 .. patternRequest.apiRequests.size-1) {
            var apiRequest = patternRequest.apiRequests.get(i)
            try {
                var client = HttpClient()
                var url = endpoint + apiRequest.path
                url = buildUrl(url, apiRequest.parameter)
                println("URL: " + url)

                with (apiRequest.method) {
                    when {
                        equals("POST") -> {
                            GlobalScope.launch {
                                println("Sending " + apiRequest.method + " to " + url + ", body: " + apiRequest.body.toString())
                                val response = client.post<HttpResponse>(url) {
                                    method = HttpMethod.Post
                                    body = TextContent(apiRequest.body.toString(), contentType = ContentType.Application.Json)
                                }
                                println("Response (" + response.status.value + "): " + response.readText())
                                client.close()
                            }
                        }
                        equals("GET") -> {
                            GlobalScope.launch {
                                println("Sending " + apiRequest.method + " to " + url + ", body: " + apiRequest.body.toString())
                                val response = client.get<HttpResponse>(url) {
                                    method = HttpMethod.Get
                                    body = TextContent(apiRequest.body.toString(), contentType = ContentType.Application.Json)
                                }
                                println("Response (" + response.status.value + "): " + response.readText())
                                client.close()
                            }
                        }
                        equals("PUT") -> {
                            GlobalScope.launch {
                                println("Sending " + apiRequest.method + " to " + url + ", body: " + apiRequest.body.toString())
                                val response = client.put<HttpResponse>(url) {
                                    method = HttpMethod.Put
                                    body = TextContent(apiRequest.body.toString(), contentType = ContentType.Application.Json)
                                }
                                println("Response (" + response.status.value + "): " + response.readText())
                                client.close()
                            }
                        }
                        equals("DELETE") -> {
                            GlobalScope.launch {
                                println("Sending " + apiRequest.method + " to " + url + ", body: " + apiRequest.body.toString())
                                val response = client.delete<HttpResponse>(url) {
                                    method = HttpMethod.Delete
                                    body = TextContent(apiRequest.body.toString(), contentType = ContentType.Application.Json)
                                }
                                println("Response (" + response.status.value + "): " + response.readText())
                                client.close()
                            }
                        }
                        else -> {
                            println("unhandled method: " + apiRequest.method)
                        }
                    }
                }
            } catch (e: Exception) {
                println("Unable to process ApiRequest :(")
            }
        }


        Thread.sleep(500)
        statisticshandler.addDone()
    }

    private fun buildUrl(url:String, parameter : Array<Pair<String, JsonElement>>) : String {
        var result = url

        //replace {pathParameter}
        while (result.contains("{") && result.contains("}")) {
            var first = result.substring(0, result.indexOf("{"))
            var paramName = result.substring(result.indexOf("{")+1, result.indexOf("}"))
            var second = result.substring(result.indexOf("}")+1)

            //Replace paramName with actual value given in parameters
            for (i in 0 .. parameter.size-1) {
                if (parameter[i].first.equals(paramName)) {
                    paramName = URLEncoder.encode(parameter[i].second.toString(),"UTF-8")
                    parameter[i] = Pair("", JsonObject())
                }
            }
            //concat together
            result = first + paramName + second
        }



        var firstParameter = true
        for (p in parameter) {
            if (p.first != "") {
                if (firstParameter) {
                    result += "?" + p.first + "=" + p.second.toString()
                    firstParameter = false
                } else {
                    result += "&" + p.first + "=" + p.second.toString()
                }
            }
        }
        return result
    }

}