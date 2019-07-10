package measurement

data class ApiRequestMeasurement (val path : String,
                               val abstractOperation : String,
                               val index: Int,
                               val start:Long,
                               val end: Long){
}
