package workload

data class ApiRequest(val path: String,
                      val parameter: Map<String, String>,
                      val method: String,
                      val body: String) {
}
