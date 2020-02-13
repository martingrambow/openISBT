package workload

import dataobjects.AbstractPattern

class PatternRequest(var id: Int, var resource: String, var abstractPattern: AbstractPattern, var apiRequests: Array<ApiRequest>)
