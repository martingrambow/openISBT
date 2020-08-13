package workload

import com.google.gson.JsonElement
import linking.ServiceLinkObject

data class ApiRequest(val path: String,
                      var parameter: Array<Pair<String, String>>,
                      var headers:Array<Pair<String, String>>,
                      val method: String,
                      var body: JsonElement,
                      var response : String?,
                      var links: ArrayList<ServiceLinkObject> = ArrayList(),
                      var status: Int) {
}
