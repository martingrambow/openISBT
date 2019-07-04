package workload

import de.tuberlin.mcc.patternconfiguration.AbstractPattern

data class PatternRequest(val id: Int,
                          val abstractPattern: AbstractPattern,
                          val apiRequests: Array<ApiRequest>) {
}
