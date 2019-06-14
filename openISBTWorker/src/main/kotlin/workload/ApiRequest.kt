package workload

import com.google.gson.JsonElement
import com.google.gson.JsonObject

data class ApiRequest(val path: String,
                      val parameter: Array<Pair<String, JsonElement>>,
                      val method: String,
                      val body: JsonElement) {
}
