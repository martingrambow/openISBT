package measurements

import java.util.*

class ApiRequestMeasurement(var path:String, var abstractOperation: String, var index: Int) {

    var start: Long = -1
    var end:Long = -1
}
