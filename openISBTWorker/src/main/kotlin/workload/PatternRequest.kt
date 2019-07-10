package workload

data class PatternRequest(val id: Int,
                          val resource : String,
                          val abstractPattern: AbstractPattern,
                          var apiRequests: Array<ApiRequest>) {
}
