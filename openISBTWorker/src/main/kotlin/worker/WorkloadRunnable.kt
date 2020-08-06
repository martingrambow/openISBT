package worker

import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.*
import io.ktor.http.content.TextContent
import kotlinx.coroutines.runBlocking
import linking.LinkController
import measurements.ApiRequestMeasurement
import measurements.PatternMeasurement
import measurements.Statisticshandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import workload.ApiRequest
import workload.PatternRequest
import java.lang.Exception

class WorkloadRunnable(var patternRequest: PatternRequest, val statisticshandler: Statisticshandler, val workerID:Int) : Runnable {

    private val log: Logger = LoggerFactory.getLogger("WorkloadRunnable")

    companion object {
        fun getController(): LinkController = LinkController()
    }

    override fun run() {

        val measurement = PatternMeasurement(patternRequest.abstractPattern.name, patternRequest.id, workerID)
        measurement.start = System.currentTimeMillis()

        log.debug("Running pattern " + patternRequest.abstractPattern.name + ": ID=" + patternRequest.id + ", " + patternRequest.apiRequests.size + " API requests")
        val abstractValues: MutableMap<String, ApiRequest> = HashMap()

        for (i in 0 until patternRequest.apiRequests.size) {

            val maxContentLen = 200

            var apiRequest = patternRequest.apiRequests[i]
            val abstractOperation = patternRequest.abstractPattern.sequence.get(i)
            log.debug("" + patternRequest.id + ": Running " + abstractOperation.operation + " against " + apiRequest.path)

            val apiMeasurement = ApiRequestMeasurement(apiRequest.path, abstractOperation.operation, i)
            apiMeasurement.start = System.currentTimeMillis()

            if (abstractOperation.input != null) {

                val dependingRequest = abstractValues[abstractOperation.input]
                if (dependingRequest != null) {

                    val adjustedRequest = getController().linkRequests(dependingRequest, apiRequest, abstractOperation)
                    if (adjustedRequest == null) {
                        log.error("Unable to link request but there should be a link")
                    } else {
                        log.debug("Successfully linked request to previous one")
                        apiRequest = adjustedRequest
                    }
                }
            }

            try {
                val client = HttpClient()
                var url = apiRequest.path
                log.debug("Path is " + url)
                url = buildUrl(url, apiRequest.parameter)
                log.debug("URL is " + url)

                with(apiRequest.method) {
                    log.info("${patternRequest.id}: Sending ${apiRequest.method} to $url, body: ${apiRequest.body}")
                    when {
                        equals("POST") -> {
                            runBlocking {
                                val response = client.post<HttpResponse>(url) {
                                    method = HttpMethod.Post
                                    //TODO: improve content type detection
                                    if (apiRequest.path == "/teams/") {
                                        body = MultiPartFormDataContent(formData {
                                            for (entry in apiRequest.body.asJsonObject.entrySet()) {
                                                append(entry.key, entry.value.toString())
                                            }
                                        })
                                        //body = TextContent(apiRequest.body.toString(), contentType = ContentType.MultiPart.FormData)
                                    } else {
                                        body = TextContent(apiRequest.body.toString(), contentType = ContentType.Application.Json)
                                    }
                                    if (apiRequest.headers != null && apiRequest.headers.isNotEmpty()) {
                                        for (h in apiRequest.headers) {
                                            log.debug("Add header: ${h.first}, ${h.second}")
                                            headers.append(h.first, h.second)
                                        }
                                    }
                                    headers.append("Accept", "application/json")
                                }
                                val responseText = response.readText()
                                var logtext = responseText
                                if (logtext.length > maxContentLen) {
                                    logtext = logtext.substring(0, maxContentLen-2) + "..."
                                }
                                log.info("${patternRequest.id}: Responded (${response.status.value}) $logtext")
                                apiRequest.response = responseText
                                apiRequest.status = response.status.value
                                client.close()
                            }
                        }
                        equals("GET") -> {
                            runBlocking {
                                val response = client.get<HttpResponse>(url) {
                                    method = HttpMethod.Get
                                    body = TextContent(apiRequest.body.toString(), contentType = ContentType.Application.Json)
                                    if (apiRequest.headers != null && apiRequest.headers.size > 0) {
                                        for (h in apiRequest.headers) {
                                            log.debug("Add header: ${h.first}, ${h.second}")
                                            headers.append(h.first, h.second)
                                        }
                                    }
                                    headers.append("Accept", "application/json")

                                }
                                val responseText = response.readText()
                                var logtext = responseText
                                if (logtext.length > maxContentLen) {
                                    logtext = logtext.substring(0, maxContentLen-2) + "..."
                                }
                                log.info("${patternRequest.id}: Responded (${response.status.value}) $logtext")
                                apiRequest.response = responseText
                                apiRequest.status = response.status.value
                                client.close()
                            }
                        }
                        equals("PUT") -> {
                            runBlocking {
                                val response = client.put<HttpResponse>(url) {
                                    method = HttpMethod.Put
                                    body = TextContent(apiRequest.body.toString(), contentType = ContentType.Application.Json)
                                    if (apiRequest.headers != null && apiRequest.headers.isNotEmpty()) {
                                        for (h in apiRequest.headers) {
                                            log.debug("Add header: ${h.first}, ${h.second}")
                                            headers.append(h.first, h.second)
                                        }
                                    }
                                    headers.append("Accept", "application/json")
                                }
                                val responseText = response.readText()
                                var logtext = responseText
                                if (logtext.length > maxContentLen) {
                                    logtext = logtext.substring(0, maxContentLen-2) + "..."
                                }
                                log.info("${patternRequest.id}: Responded (${response.status.value}) $logtext")
                                apiRequest.response = responseText
                                apiRequest.status = response.status.value
                                client.close()
                            }
                        }
                        equals("PATCH") -> {
                            runBlocking {
                                val response = client.patch<HttpResponse>(url) {
                                    method = HttpMethod.Patch
                                    body = TextContent(apiRequest.body.toString(), contentType = ContentType.Application.Json)
                                    if (apiRequest.headers != null && apiRequest.headers.isNotEmpty()) {
                                        for (h in apiRequest.headers) {
                                            log.debug("Add header: ${h.first}, ${h.second}")
                                            headers.append(h.first, h.second)
                                        }
                                    }
                                    headers.append("Accept", "application/json")
                                }
                                val responseText = response.readText()
                                var logtext = responseText
                                if (logtext.length > maxContentLen) {
                                    logtext = logtext.substring(0, maxContentLen-2) + "..."
                                }
                                log.info("${patternRequest.id}: Responded (${response.status.value}) $logtext")
                                apiRequest.response = responseText
                                apiRequest.status = response.status.value
                                client.close()
                            }
                        }
                        equals("DELETE") -> {
                            runBlocking {
                                val response = client.delete<HttpResponse>(url) {
                                    method = HttpMethod.Delete
                                    body = TextContent(apiRequest.body.toString(), contentType = ContentType.Application.Json)
                                    if (apiRequest.headers != null && apiRequest.headers.isNotEmpty()) {
                                        for (h in apiRequest.headers) {
                                            log.debug("Add header: ${h.first}, ${h.second}")
                                            headers.append(h.first, h.second)
                                        }
                                    }
                                    headers.append("Accept", "application/json")

                                }
                                val responseText = response.readText()
                                var logtext = responseText
                                if (logtext.length > maxContentLen) {
                                    logtext = logtext.substring(0, maxContentLen-2) + "..."
                                }
                                log.info("${patternRequest.id}: Responded (${response.status.value}) $logtext")
                                apiRequest.response = responseText
                                apiRequest.status = response.status.value
                                client.close()
                            }
                        }
                        else -> {
                            println("unhandled method: ${apiRequest.method}")
                        }
                    }
                }
            } catch (e: Exception) {
                log.error("Unable to process ApiRequest :(")
                e.printStackTrace()
            }

            if (abstractOperation.output != null) {
                abstractValues[abstractOperation.output] = apiRequest
            }

            apiMeasurement.end = System.currentTimeMillis()
            measurement.apiRequestMeasurements.add(apiMeasurement)
        }
        measurement.end = System.currentTimeMillis()
        statisticshandler.addMeasurement(measurement)
        log.debug("Created Measurement: ${GsonBuilder().create().toJson(measurement)}")
        statisticshandler.addDone()
    }

    private fun buildUrl(url:String, parameter : Array<Pair<String, String>>) : String {
        var result = url

        //replace {pathParameter}
        while (result.contains("{") && result.contains("}")) {
            val first = result.substring(0, result.indexOf("{"))
            var paramName = result.substring(result.indexOf("{")+1, result.indexOf("}"))
            val second = result.substring(result.indexOf("}")+1)

            //Replace paramName with actual value given in parameters
            for (i in 0 until parameter.size) {
                if (parameter[i].first == paramName) {
                    paramName = parameter[i].second
                    //paramName = URLEncoder.encode(parameter[i].second.toString(),"UTF-8")
                    parameter[i] = Pair("", "")
                }
            }
            //concat together
            result = "$first$paramName$second"
        }



        var firstParameter = true
        for (p in parameter) {
            if (p.first != "") {
                if (firstParameter) {
                    result += "?" + p.first + "=" + p.second
                    firstParameter = false
                } else {
                    result += "&" + p.first + "=" + p.second
                }
            }
        }
        return result
    }

}