package measurement

data class ApiRequestMeasurement (var path : String,
                                  val abstractOperation : String,
                                  val index: Int,
                                  val start:Long,
                                  val end: Long){
}
