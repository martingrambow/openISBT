package workload

import com.google.gson.JsonElement

class ApiRequest {
    var path:String = ""
    var parameter: Array<Pair<String, String>> = arrayOf()
    var headers:Array<Pair<String, String>> = arrayOf()
    var method: String = ""
    var body: JsonElement? = null
    var contentType: String = ""
}
