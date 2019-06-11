package workload

data class PatternRequest(val id: Int,
                          val abstractPatternName: String,
                          val apiRequests: Array<ApiRequest>) {
}
