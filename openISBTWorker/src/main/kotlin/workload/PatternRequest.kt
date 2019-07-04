package workload

data class PatternRequest(val id: Int,
                          val abstractPattern: AbstractPattern,
                          val apiRequests: Array<ApiRequest>) {
}
