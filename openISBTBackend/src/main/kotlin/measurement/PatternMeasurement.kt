package measurement

data class PatternMeasurement (val resource : String,
                             val patternName : String,
                             val requestID: Int,
                             val workerID:Int,
                             val start:Long,
                             val end: Long,
                             val apiRequestMeasurements: Array<ApiRequestMeasurement>){
}