package workload

import com.google.gson.JsonElement
import io.ktor.http.ContentType

data class ApiRequest(val path: String,
                      val parameter: Array<Pair<String, JsonElement>>,
                      val headers: Array<Pair<String, String>>,
                      val method: String,
                      val body: JsonElement) {
}
