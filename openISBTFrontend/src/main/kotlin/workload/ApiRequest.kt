package workload

import kotlin.js.Json

class ApiRequest() {
    var path:String = ""
    var parameter: ArrayList<Pair<String, Json>> = ArrayList()
    var method: String = ""
    var body: Json? = null
}
