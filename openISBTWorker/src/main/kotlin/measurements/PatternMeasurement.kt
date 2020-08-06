package measurements

import kotlin.collections.ArrayList

class PatternMeasurement(var patternName : String, var requestID : Int, var workerID : Int) {

    var start: Long = -1
    var end:Long = -1
    var apiRequestMeasurements:ArrayList<ApiRequestMeasurement> = ArrayList()
}