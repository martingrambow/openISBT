package workload

data class PatternRequest(val requestID: Int,
                          val aPatternName: String,
                          val apiRequests: Array<ApiRequest>) {
}
