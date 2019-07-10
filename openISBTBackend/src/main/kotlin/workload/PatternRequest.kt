package workload

import de.tuberlin.mcc.patternconfiguration.AbstractPattern

data class PatternRequest(val id: Int,
                          val resource : String,
                          val abstractPattern: AbstractPattern,
                          val apiRequests: Array<ApiRequest>) {
}
