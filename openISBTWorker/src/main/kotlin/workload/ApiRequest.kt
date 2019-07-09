package workload

import com.google.gson.JsonElement
import io.ktor.client.response.HttpResponse
import io.ktor.http.ContentType

data class ApiRequest(val path: String,
                      var parameter: Array<Pair<String, String>>,
                      var headers:Array<Pair<String, String>>,
                      val method: String,
                      var body: JsonElement,
                      var response : String,
                      var status: Int) {
}
