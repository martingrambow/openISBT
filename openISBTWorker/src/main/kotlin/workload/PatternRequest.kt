package workload

data class PatternRequest(val id: Int,
                          val abstractPattern: AbstractPattern,
                          var apiRequests: Array<ApiRequest>) {
}
